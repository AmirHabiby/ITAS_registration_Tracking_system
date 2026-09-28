import {
  Alert,
  Button,
  Card,
  DatePicker,
  Descriptions,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import dayjs, { type Dayjs } from "dayjs";
import { portalService, type Training } from "../../services/portalService";
import {
  assessmentService,
  type AssessmentDraft,
  type CompletedAssessmentAttempt,
  type GradingQueueItem,
  type InstituteAssessment,
  type InstituteAssessmentQuestion,
  type WrittenGrading,
} from "../../services/assessmentService";
import { getAssessmentPublishIssues } from "./assessmentPublishValidation";

type SettingsValues = Omit<AssessmentDraft, "availableFrom" | "availableUntil"> & {
  availability?: [Dayjs | null, Dayjs | null];
};

type QuestionValues = {
  prompt: string;
  questionType: "MULTIPLE_CHOICE" | "WRITTEN_RESPONSE";
  points: number;
  gradingRubric?: string;
  options?: { id?: string; text: string; correct: boolean }[];
};

const formatError = (error: unknown, fallback: string) => {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { data?: { message?: string; detail?: string } } }).response;
    if (response?.data?.message) return response.data.message;
    if (response?.data?.detail) return response.data.detail;
  }
  return fallback;
};

const isNotFound = (error: unknown) =>
  typeof error === "object" && error !== null && "response" in error &&
  (error as { response?: { status?: number } }).response?.status === 404;

function toDraft(values: SettingsValues): AssessmentDraft {
  return {
    title: values.title.trim(),
    instructions: values.instructions?.trim() ?? "",
    passingScore: values.passingScore,
    durationMinutes: values.durationMinutes,
    attemptLimit: values.attemptLimit,
    availableFrom: values.availability?.[0]?.toISOString() ?? null,
    availableUntil: values.availability?.[1]?.toISOString() ?? null,
    randomizeQuestions: values.randomizeQuestions,
    randomizeOptions: values.randomizeOptions,
  };
}

function statusColor(status: string) {
  if (status === "PUBLISHED" || status === "COMPLETED") return "green";
  if (status === "DRAFT") return "gold";
  return "default";
}

export function InstituteAssessmentsPage() {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [trainingId, setTrainingId] = useState<string>();
  const [assessments, setAssessments] = useState<InstituteAssessment[]>([]);
  const [queue, setQueue] = useState<GradingQueueItem[]>([]);
  const [completedAttempts, setCompletedAttempts] = useState<CompletedAssessmentAttempt[]>([]);
  const [loading, setLoading] = useState(true);
  const [queueLoading, setQueueLoading] = useState(false);
  const [completedLoading, setCompletedLoading] = useState(false);
  const [releasingAttemptId, setReleasingAttemptId] = useState<string | null>(null);
  const [releaseTarget, setReleaseTarget] = useState<CompletedAssessmentAttempt | null>(null);
  const [releaseDialogOpen, setReleaseDialogOpen] = useState(false);
  const [publishingAssessmentId, setPublishingAssessmentId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [settingsSaving, setSettingsSaving] = useState(false);
  const [settingsTarget, setSettingsTarget] = useState<InstituteAssessment | null>(null);
  const [publishTarget, setPublishTarget] = useState<InstituteAssessment | null>(null);
  const [editingAssessment, setEditingAssessment] = useState<InstituteAssessment | null>(null);
  const [preview, setPreview] = useState<InstituteAssessment | null>(null);
  const [questionOpen, setQuestionOpen] = useState(false);
  const [questionSaving, setQuestionSaving] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<InstituteAssessmentQuestion | null>(null);
  const [gradingTarget, setGradingTarget] = useState<GradingQueueItem | null>(null);
  const [writtenGrading, setWrittenGrading] = useState<WrittenGrading | null>(null);
  const [gradingLoading, setGradingLoading] = useState(false);
  const [gradingBusy, setGradingBusy] = useState(false);
  const [finalized, setFinalized] = useState(false);
  const [released, setReleased] = useState(false);
  const [settingsForm] = Form.useForm<SettingsValues>();
  const [questionForm] = Form.useForm<QuestionValues>();
  const questionType = Form.useWatch("questionType", questionForm);

  const reloadAssessments = useCallback(async (id = trainingId) => {
    if (!id) {
      setAssessments([]);
      return;
    }
    setAssessments((await assessmentService.listInstituteAssessments(id)).data);
  }, [trainingId]);

  const reloadQueue = useCallback(async () => {
    setQueueLoading(true);
    setCompletedLoading(true);
    try {
      const [nextQueue, nextCompleted] = await Promise.all([
        assessmentService.listGradingQueue(),
        assessmentService.listCompletedAttempts(),
      ]);
      setQueue(nextQueue.data);
      setCompletedAttempts(nextCompleted.data);
    } catch (loadError) {
      setError(formatError(loadError, "Unable to load assessment submissions."));
    } finally {
      setQueueLoading(false);
      setCompletedLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    Promise.allSettled([
      portalService.listInstituteTrainings(),
      assessmentService.listGradingQueue(),
      assessmentService.listCompletedAttempts(),
    ])
      .then(async ([trainingResult, queueResult, completedResult]) => {
        if (!active) return;
        if (trainingResult.status === "rejected") {
          throw trainingResult.reason;
        }
        const nextTrainings = trainingResult.value.data;
        setTrainings(nextTrainings);
        if (queueResult.status === "fulfilled") setQueue(queueResult.value.data);
        else setError(formatError(queueResult.reason, "Unable to load the grading queue."));
        if (completedResult.status === "fulfilled") setCompletedAttempts(completedResult.value.data);
        else setError(formatError(completedResult.reason, "Unable to load completed attempts."));
        const selected = nextTrainings[0]?.id;
        setTrainingId(selected);
        if (selected) {
          const results = await Promise.all(nextTrainings.map((training) =>
            assessmentService.listInstituteAssessments(training.id),
          ));
          if (active) setAssessments(results.flatMap((result) => result.data));
        }
      })
      .catch((loadError: unknown) => {
        if (active) setError(formatError(loadError, "Unable to load assessment data."));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  const selectedTraining = useMemo(
    () => trainings.find((training) => training.id === trainingId),
    [trainings, trainingId],
  );

  async function selectTraining(id: string) {
    setTrainingId(id);
    setLoading(true);
    try {
      setAssessments((await assessmentService.listInstituteAssessments(id)).data);
      setError(null);
    } catch (loadError) {
      setError(formatError(loadError, "Unable to load this course's assessments."));
    } finally {
      setLoading(false);
    }
  }

  function openCreateSettings() {
    setSettingsTarget(null);
    settingsForm.resetFields();
    if (selectedTraining?.passingScore !== null && selectedTraining?.passingScore !== undefined) {
      settingsForm.setFieldValue("passingScore", selectedTraining.passingScore);
    }
    setSettingsOpen(true);
  }

  function openEditSettings(assessment: InstituteAssessment) {
    setSettingsTarget(assessment);
    settingsForm.setFieldsValue({
      title: assessment.title,
      instructions: assessment.instructions,
      passingScore: assessment.passingScore,
      durationMinutes: assessment.durationMinutes,
      attemptLimit: assessment.attemptLimit,
      availability: [
        assessment.availableFrom ? dayjs(assessment.availableFrom) : null,
        assessment.availableUntil ? dayjs(assessment.availableUntil) : null,
      ],
      randomizeQuestions: assessment.randomizeQuestions,
      randomizeOptions: assessment.randomizeOptions,
    });
    setSettingsOpen(true);
  }

  async function saveSettings(values: SettingsValues) {
    if (!trainingId) return;
    setSettingsSaving(true);
    try {
      if (settingsTarget) {
        await assessmentService.updateAssessment(settingsTarget.id, toDraft(values));
        message.success("Assessment settings saved.");
      } else {
        await assessmentService.createAssessment(trainingId, toDraft(values));
        message.success("Draft assessment created.");
      }
      setSettingsOpen(false);
      await reloadAssessments();
    } catch (saveError) {
      message.error(formatError(saveError, "Unable to save assessment settings."));
    } finally {
      setSettingsSaving(false);
    }
  }

  async function openQuestions(assessment: InstituteAssessment) {
    try {
      const response = await assessmentService.previewAssessment(assessment.id);
      setEditingAssessment(response.data);
    } catch (loadError) {
      message.error(formatError(loadError, "Unable to load the question editor."));
    }
  }

  function openNewQuestion() {
    setEditingQuestion(null);
    questionForm.resetFields();
    questionForm.setFieldsValue({ questionType: "MULTIPLE_CHOICE", options: [{ text: "", correct: true }, { text: "", correct: false }] });
    setQuestionOpen(true);
  }

  function openEditQuestion(question: InstituteAssessmentQuestion) {
    setEditingQuestion(question);
    questionForm.setFieldsValue({
      prompt: question.prompt,
      questionType: question.questionType,
      points: question.points,
      gradingRubric: question.gradingRubric ?? "",
      options: question.options.map((option) => ({
        id: option.id,
        text: option.text,
        correct: option.correct,
      })),
    });
    setQuestionOpen(true);
  }

  async function refreshEditor() {
    if (!editingAssessment) return;
    const updated = (await assessmentService.previewAssessment(editingAssessment.id)).data;
    setEditingAssessment(updated);
    await reloadAssessments();
  }

  async function reconcileOptions(
    assessmentId: string,
    questionId: string,
    oldOptions: InstituteAssessmentQuestion["options"],
    options: NonNullable<QuestionValues["options"]>,
  ) {
    const keptIds = new Set(options.flatMap((option) => option.id ? [option.id] : []));
    for (const oldOption of oldOptions) {
      if (!keptIds.has(oldOption.id)) {
        await assessmentService.deleteOption(assessmentId, questionId, oldOption.id);
      }
    }
    for (const [index, option] of options.entries()) {
      const body = { text: option.text.trim(), displayOrder: index + 1, correct: option.correct };
      if (option.id) await assessmentService.updateOption(assessmentId, questionId, option.id, body);
      else await assessmentService.addOption(assessmentId, questionId, body);
    }
  }

  async function saveQuestion(values: QuestionValues) {
    if (!editingAssessment) return;
    setQuestionSaving(true);
    try {
      const order = editingQuestion?.displayOrder ?? editingAssessment.questions.length + 1;
      const body = {
        prompt: values.prompt.trim(),
        questionType: values.questionType,
        displayOrder: order,
        points: values.points,
        gradingRubric: values.questionType === "WRITTEN_RESPONSE" ? values.gradingRubric?.trim() ?? "" : null,
      };
      if (editingQuestion) {
        if (editingQuestion.questionType === "MULTIPLE_CHOICE" && values.questionType === "WRITTEN_RESPONSE") {
          for (const option of editingQuestion.options) {
            await assessmentService.deleteOption(editingAssessment.id, editingQuestion.id, option.id);
          }
          await assessmentService.updateQuestion(editingAssessment.id, editingQuestion.id, body);
        } else {
          await assessmentService.updateQuestion(editingAssessment.id, editingQuestion.id, body);
        }
        if (values.questionType === "MULTIPLE_CHOICE") {
          await reconcileOptions(editingAssessment.id, editingQuestion.id, editingQuestion.options, values.options ?? []);
        }
      } else {
        const response = await assessmentService.addQuestion(editingAssessment.id, body);
        const created = response.data.questions.find((question) => question.displayOrder === order);
        if (created && values.questionType === "MULTIPLE_CHOICE") {
          await reconcileOptions(editingAssessment.id, created.id, [], values.options ?? []);
        }
      }
      setQuestionOpen(false);
      await refreshEditor();
      message.success(editingQuestion ? "Question updated." : "Question added.");
    } catch (saveError) {
      message.error(formatError(saveError, "Unable to save the question."));
    } finally {
      setQuestionSaving(false);
    }
  }

  async function removeQuestion(question: InstituteAssessmentQuestion) {
    if (!editingAssessment) return;
    try {
      await assessmentService.deleteQuestion(editingAssessment.id, question.id);
      await refreshEditor();
      message.success("Question removed.");
    } catch (deleteError) {
      message.error(formatError(deleteError, "Unable to remove the question."));
    }
  }

  async function moveQuestion(index: number, direction: -1 | 1) {
    if (!editingAssessment) return;
    const questions = [...editingAssessment.questions].sort((a, b) => a.displayOrder - b.displayOrder);
    const target = index + direction;
    if (target < 0 || target >= questions.length) return;
    [questions[index], questions[target]] = [questions[target], questions[index]];
    try {
      const updated = await assessmentService.reorderQuestions(editingAssessment.id, questions.map((question) => question.id));
      setEditingAssessment(updated.data);
      await reloadAssessments();
    } catch (reorderError) {
      message.error(formatError(reorderError, "Unable to reorder questions."));
    }
  }

  async function startRevision(assessment: InstituteAssessment) {
    try {
      const revision = await assessmentService.createRevision(assessment.id);
      await reloadAssessments();
      setEditingAssessment(revision.data);
      message.success(`Draft version ${revision.data.version} created.`);
    } catch (revisionError) {
      message.error(formatError(revisionError, "Unable to create a draft revision."));
    }
  }

  async function publishAssessment(assessment: InstituteAssessment) {
    setPublishingAssessmentId(assessment.id);
    try {
      const published = (await assessmentService.publishAssessment(assessment.id)).data;
      setAssessments((current) => current.map((item) => item.id === published.id ? published : item));
      message.success("Assessment published.");
      try {
        await reloadAssessments();
      } catch (reloadError) {
        setError(`Assessment was published, but the list could not be refreshed: ${formatError(reloadError, "Refresh the page to see the updated status.")}`);
      }
    } catch (publishError) {
      const reason = formatError(publishError, "Unable to publish assessment.");
      setError(`Assessment remains a draft. ${reason}`);
      message.error(reason);
    } finally {
      setPublishingAssessmentId(null);
    }
  }

  async function openGrading(item: GradingQueueItem) {
    setGradingTarget(item);
    setWrittenGrading(null);
    setFinalized(false);
    setReleased(false);
    setGradingLoading(true);
    try {
      const grading = await assessmentService.getWrittenGrading(item.attemptId);
      let hasFinalResult = false;
      let resultReleasedAt: string | null = null;
      try {
        const result = await assessmentService.getInstituteResult(item.attemptId);
        hasFinalResult = true;
        resultReleasedAt = result.data.releasedAt;
      } catch (resultError) {
        if (!isNotFound(resultError)) throw resultError;
      }
      setWrittenGrading(grading.data);
      setFinalized(hasFinalResult);
      setReleased(Boolean(resultReleasedAt));
    } catch (loadError) {
      message.error(formatError(loadError, "Unable to load written answers."));
      setGradingTarget(null);
    } finally {
      setGradingLoading(false);
    }
  }

  async function submitGrade(questionId: string, values: { awardedMarks: number; feedback?: string }) {
    if (!gradingTarget) return;
    setGradingBusy(true);
    try {
      const response = await assessmentService.gradeWrittenQuestion(gradingTarget.attemptId, questionId, {
        awardedMarks: values.awardedMarks,
        feedback: values.feedback ?? "",
      });
      setWrittenGrading(response.data);
      setFinalized(false);
      await reloadQueue();
      message.success("Grade saved.");
    } catch (gradeError) {
      message.error(formatError(gradeError, "Unable to save this grade."));
    } finally {
      setGradingBusy(false);
    }
  }

  async function finalizeGrading() {
    if (!gradingTarget) return;
    setGradingBusy(true);
    try {
      await assessmentService.finalizeGrading(gradingTarget.attemptId);
      setFinalized(true);
      message.success("Final grading saved.");
    } catch (finalizeError) {
      message.error(formatError(finalizeError, "Unable to finalize grading. Make sure all written answers are graded."));
    } finally {
      setGradingBusy(false);
    }
  }

  async function releaseResult() {
    if (!gradingTarget) return false;
    setGradingBusy(true);
    try {
      await assessmentService.releaseResult(gradingTarget.attemptId);
      setReleased(true);
      await reloadQueue();
      message.success("Result released to the representative.");
      return true;
    } catch (releaseError) {
      message.error(formatError(releaseError, "Unable to release this result."));
      return false;
    } finally {
      setGradingBusy(false);
    }
  }

  async function releaseCompletedResult(item: CompletedAssessmentAttempt) {
    setReleasingAttemptId(item.attemptId);
    try {
      await assessmentService.releaseResult(item.attemptId);
      message.success("Result released to the representative.");
      await reloadQueue();
      return true;
    } catch (releaseError) {
      message.error(formatError(releaseError, "Unable to release this result."));
      return false;
    } finally {
      setReleasingAttemptId(null);
    }
  }

  const assessmentColumns = [
    { title: "Assessment", dataIndex: "title", key: "title" },
    { title: "Version", dataIndex: "version", key: "version" },
    { title: "Duration", dataIndex: "durationMinutes", key: "duration", render: (value: number) => `${value} min` },
    { title: "Questions", dataIndex: "questions", key: "questions", render: (questions: InstituteAssessment["questions"]) => questions.length },
    { title: "Status", dataIndex: "status", key: "status", render: (value: string) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, assessment: InstituteAssessment) => (
        <Space wrap>
          <Button onClick={() => void openQuestions(assessment)}>Questions</Button>
          <Button onClick={() => void assessmentService.previewAssessment(assessment.id)
            .then((response) => setPreview(response.data))
            .catch((previewError: unknown) => message.error(formatError(previewError, "Unable to preview assessment.")))}>
            Preview
          </Button>
          {assessment.status === "DRAFT" ? (
            <>
              <Button onClick={() => openEditSettings(assessment)}>Edit settings</Button>
              {(() => {
                const publishIssues = getAssessmentPublishIssues(assessment);
                return (
                  <Space direction="vertical" size={4}>
                    <Button
                      type="primary"
                      loading={publishingAssessmentId === assessment.id}
                      disabled={publishingAssessmentId !== null}
                      onClick={() => setPublishTarget(assessment)}
                    >Publish</Button>
                    {publishIssues.length > 0 && (
                      <Typography.Text type="secondary" style={{ maxWidth: 260, fontSize: 12 }}>
                        {publishIssues.join(" ")}
                      </Typography.Text>
                    )}
                  </Space>
                );
              })()}
            </>
          ) : (
            <Button onClick={() => void startRevision(assessment)}>Create revision</Button>
          )}
        </Space>
      ),
    },
  ];

  const questionColumns = [
    { title: "#", dataIndex: "displayOrder", key: "order", width: 60 },
    { title: "Question", dataIndex: "prompt", key: "prompt" },
    { title: "Type", dataIndex: "questionType", key: "type" },
    { title: "Marks", dataIndex: "points", key: "points" },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, question: InstituteAssessmentQuestion, index: number) => editingAssessment?.status !== "DRAFT" ? (
        <Typography.Text type="secondary">Published versions are read-only</Typography.Text>
      ) : (
        <Space wrap>
          <Button size="small" disabled={index === 0} onClick={() => void moveQuestion(index, -1)}>Up</Button>
          <Button size="small" disabled={index === (editingAssessment?.questions.length ?? 0) - 1} onClick={() => void moveQuestion(index, 1)}>Down</Button>
          <Button size="small" onClick={() => openEditQuestion(question)}>Edit</Button>
          <Button size="small" danger onClick={() => Modal.confirm({
            title: "Delete this question?",
            content: "This draft question and its options will be removed.",
            onOk: () => removeQuestion(question),
          })}>Delete</Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      {error && <Alert type="error" showIcon message={error} closable onClose={() => setError(null)} />}
      <Tabs items={[
        {
          key: "assessments",
          label: "Assessments",
          children: (
            <Space direction="vertical" size="middle" style={{ width: "100%" }}>
              <Card title="Assessment authoring" extra={
                <Space wrap>
                  <Select
                    aria-label="Training course"
                    value={trainingId}
                    placeholder="Select a course"
                    style={{ minWidth: 230 }}
                    options={trainings.map((training) => ({ value: training.id, label: training.title }))}
                    onChange={(id) => void selectTraining(id)}
                  />
                  <Button type="primary" disabled={!trainingId} onClick={openCreateSettings}>Create assessment</Button>
                </Space>
              }>
                {selectedTraining && <Typography.Text type="secondary">{selectedTraining.title}</Typography.Text>}
                <Table
                  style={{ marginTop: 16 }}
                  loading={loading}
                  dataSource={assessments}
                  rowKey="id"
                  locale={{ emptyText: trainings.length ? "No assessments for this course yet." : "Create a training course before authoring an assessment." }}
                  columns={assessmentColumns}
                  pagination={{ pageSize: 8 }}
                  scroll={{ x: 850 }}
                />
              </Card>
            </Space>
          ),
        },
        {
          key: "grading",
          label: `Grading queue${queue.length ? ` (${queue.length})` : ""}`,
          children: (
            <Card title="Submitted written assessments">
              <Table
                loading={queueLoading}
                dataSource={queue}
                rowKey="attemptId"
                locale={{ emptyText: "No submitted written answers need grading." }}
                columns={[
                  { title: "Representative", dataIndex: "representativeName", key: "representative" },
                  { title: "Assessment", dataIndex: "assessmentTitle", key: "assessment" },
                  { title: "Attempt", dataIndex: "attemptNumber", key: "attemptNumber" },
                  { title: "Attempt status", dataIndex: "attemptStatus", key: "attemptStatus", render: (value: string) => <Tag>{value}</Tag> },
                  { title: "Grading", key: "progress", render: (_: unknown, item: GradingQueueItem) => `${item.gradedWrittenQuestionCount}/${item.writtenQuestionCount}` },
                  { title: "Submitted", dataIndex: "submittedAt", key: "submittedAt", render: (value: string | null) => value ? new Date(value).toLocaleString() : "Expired" },
                  { title: "Action", key: "action", render: (_: unknown, item: GradingQueueItem) => <Button type="primary" onClick={() => void openGrading(item)}>Review and grade</Button> },
                ]}
                pagination={{ pageSize: 8 }}
                scroll={{ x: 760 }}
              />
            </Card>
          ),
        },
        {
          key: "completed",
          label: `Completed attempts${completedAttempts.length ? ` (${completedAttempts.length})` : ""}`,
          children: (
            <Card title="Completed submissions and result release">
              <Table
                loading={completedLoading}
                dataSource={completedAttempts}
                rowKey="attemptId"
                locale={{ emptyText: "Completed submissions will appear here." }}
                columns={[
                  { title: "Representative", dataIndex: "representativeName", key: "representative" },
                  { title: "Assessment", dataIndex: "assessmentTitle", key: "assessment" },
                  { title: "Attempt", dataIndex: "attemptNumber", key: "attempt" },
                  { title: "Status", dataIndex: "attemptStatus", key: "status", render: (value: string) => <Tag color={value === "SUBMITTED" ? "blue" : "default"}>{value}</Tag> },
                  { title: "Score", key: "score", render: (_: unknown, item: CompletedAssessmentAttempt) => item.awardedMarks === null || item.maximumMarks === null ? "Pending written grading" : `${item.awardedMarks}/${item.maximumMarks} (${item.percentage}%)` },
                  {
                    title: "Result status",
                    key: "resultStatus",
                    render: (_: unknown, item: CompletedAssessmentAttempt) => item.releasedAt
                      ? <Tag color="green">Released</Tag>
                      : item.finalized ? <Tag color="gold">Ready to release</Tag> : <Tag>Grading pending</Tag>,
                  },
                  {
                    title: "Action",
                    key: "action",
                    render: (_: unknown, item: CompletedAssessmentAttempt) => item.finalized && !item.releasedAt
                      ? <Button
                        type="primary"
                        loading={releasingAttemptId === item.attemptId}
                        disabled={releasingAttemptId !== null}
                        onClick={() => setReleaseTarget(item)}
                      >Release result</Button>
                      : item.writtenQuestionCount > item.gradedWrittenQuestionCount
                        ? <Button onClick={() => void openGrading({
                          attemptId: item.attemptId,
                          assessmentId: item.assessmentId,
                          assessmentTitle: item.assessmentTitle,
                          representativeName: item.representativeName,
                          attemptNumber: item.attemptNumber,
                          attemptStatus: item.attemptStatus,
                          submittedAt: item.submittedAt,
                          expiredAt: item.expiredAt,
                          writtenQuestionCount: item.writtenQuestionCount,
                          gradedWrittenQuestionCount: item.gradedWrittenQuestionCount,
                        })}>Continue grading</Button>
                        : "—",
                  },
                ]}
                pagination={{ pageSize: 8 }}
                scroll={{ x: 1000 }}
              />
            </Card>
          ),
        },
      ]} />

      <Modal
        title="Publish assessment?"
        open={Boolean(publishTarget)}
        onCancel={() => {
          if (!publishingAssessmentId) setPublishTarget(null);
        }}
        onOk={async () => {
          if (!publishTarget) return;
          await publishAssessment(publishTarget);
          setPublishTarget(null);
        }}
        okText="Publish"
        confirmLoading={Boolean(publishTarget && publishingAssessmentId === publishTarget.id)}
        okButtonProps={{ disabled: !publishTarget || publishingAssessmentId !== null }}
        cancelButtonProps={{ disabled: publishingAssessmentId !== null }}
        closable={publishingAssessmentId === null}
        maskClosable={publishingAssessmentId === null}
      >
        <Space direction="vertical" style={{ width: "100%" }}>
          <Typography.Paragraph>
            Publishing creates an immutable version that representatives can use for attempts.
          </Typography.Paragraph>
          {publishTarget && getAssessmentPublishIssues(publishTarget).length > 0 && (
            <Alert
              type="warning"
              showIcon
              message="Check the assessment before publishing"
              description={getAssessmentPublishIssues(publishTarget).map((issue) => (
                <div key={issue}>{issue}</div>
              ))}
            />
          )}
        </Space>
      </Modal>

      <Modal
        title={settingsTarget ? "Edit draft settings" : "Create assessment"}
        open={settingsOpen}
        onCancel={() => setSettingsOpen(false)}
        onOk={() => settingsForm.submit()}
        confirmLoading={settingsSaving}
        okText={settingsTarget ? "Save settings" : "Create draft"}
        destroyOnClose
      >
        <Form form={settingsForm} layout="vertical" onFinish={(values) => void saveSettings(values)}>
          <Form.Item name="title" label="Assessment title" rules={[{ required: true }, { max: 180 }]}>
            <Input maxLength={180} />
          </Form.Item>
          <Form.Item name="instructions" label="Instructions" rules={[{ max: 2000 }]}>
            <Input.TextArea rows={3} maxLength={2000} />
          </Form.Item>
          <Space size="middle" wrap style={{ width: "100%" }}>
            <Form.Item name="durationMinutes" label="Duration (minutes)" rules={[{ required: true }]}><InputNumber min={1} max={1440} /></Form.Item>
            <Form.Item name="passingScore" label="Pass mark (%)" rules={[{ required: true }]}><InputNumber min={0} max={100} precision={2} /></Form.Item>
            <Form.Item name="attemptLimit" label="Attempt limit" rules={[{ required: true }]}><InputNumber min={1} max={100} /></Form.Item>
          </Space>
          <Form.Item name="availability" label="Availability window">
            <DatePicker.RangePicker showTime style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="randomizeQuestions" label="Randomize questions">
            <Select options={[{ value: true, label: "Yes" }, { value: false, label: "No" }]} />
          </Form.Item>
          <Form.Item name="randomizeOptions" label="Randomize MCQ options">
            <Select options={[{ value: true, label: "Yes" }, { value: false, label: "No" }]} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingAssessment ? `Questions — ${editingAssessment.title} (v${editingAssessment.version})` : "Questions"}
        open={Boolean(editingAssessment)}
        width={900}
        onCancel={() => setEditingAssessment(null)}
        footer={editingAssessment?.status === "DRAFT"
          ? <Button type="primary" onClick={openNewQuestion}>Add question</Button>
          : <Button onClick={() => setEditingAssessment(null)}>Close</Button>}
      >
        {editingAssessment && (
          <Table
            dataSource={[...editingAssessment.questions].sort((a, b) => a.displayOrder - b.displayOrder)}
            rowKey="id"
            columns={questionColumns}
            pagination={false}
            locale={{ emptyText: "Add questions to complete this assessment." }}
            scroll={{ x: 700 }}
          />
        )}
      </Modal>

      <Modal
        title={editingQuestion ? "Edit question" : "Add question"}
        open={questionOpen}
        onCancel={() => setQuestionOpen(false)}
        onOk={() => questionForm.submit()}
        confirmLoading={questionSaving}
        width={720}
        destroyOnClose
      >
        <Form
          form={questionForm}
          layout="vertical"
          onValuesChange={(changed) => {
            if (changed.questionType === "WRITTEN_RESPONSE") questionForm.setFieldValue("options", []);
            if (changed.questionType === "MULTIPLE_CHOICE" && !questionForm.getFieldValue("options")?.length) {
              questionForm.setFieldValue("options", [{ text: "", correct: true }, { text: "", correct: false }]);
            }
          }}
          onFinish={(values) => void saveQuestion(values)}
        >
          <Form.Item name="prompt" label="Question" rules={[{ required: true }, { max: 4000 }]}>
            <Input.TextArea rows={3} maxLength={4000} />
          </Form.Item>
          <Space wrap>
            <Form.Item name="questionType" label="Type" rules={[{ required: true }]}>
              <Select style={{ minWidth: 220 }} options={[
                { value: "MULTIPLE_CHOICE", label: "Single-answer MCQ" },
                { value: "WRITTEN_RESPONSE", label: "Written response" },
              ]} />
            </Form.Item>
            <Form.Item name="points" label="Maximum marks" rules={[{ required: true }]}>
              <InputNumber min={0.01} max={99999} precision={2} />
            </Form.Item>
          </Space>
          {questionType === "WRITTEN_RESPONSE" ? (
            <Form.Item name="gradingRubric" label="Grading rubric" rules={[{ required: true }, { max: 4000 }]}>
              <Input.TextArea rows={4} maxLength={4000} />
            </Form.Item>
          ) : (
            <Form.List name="options" rules={[{
              validator: async (_, options: QuestionValues["options"]) => {
                if (!options || options.length < 2) throw new Error("Provide at least two answer options.");
                if (options.some((option) => !option?.text?.trim())) throw new Error("Every option needs text.");
                if (options.filter((option) => option.correct).length !== 1) throw new Error("Mark exactly one correct answer.");
              },
            }]}>
              {(fields, { add, remove }, meta) => (
                <div>
                  <Typography.Text strong>Answer options (mark exactly one correct)</Typography.Text>
                  {fields.map((field, index) => (
                    <Space key={field.key} align="baseline" style={{ display: "flex", marginTop: 8 }}>
                      <Form.Item name={[field.name, "text"]} rules={[{ required: true, whitespace: true }]}>
                        <Input aria-label={`Option ${index + 1}`} placeholder={`Option ${index + 1}`} maxLength={2000} />
                      </Form.Item>
                      <Form.Item name={[field.name, "correct"]}>
                        <Select aria-label={`Correct answer for option ${index + 1}`} style={{ width: 130 }} options={[
                          { value: true, label: "Correct" },
                          { value: false, label: "Incorrect" },
                        ]} />
                      </Form.Item>
                      <Button danger onClick={() => remove(field.name)}>Remove</Button>
                    </Space>
                  ))}
                  <Button onClick={() => add({ text: "", correct: false })}>Add option</Button>
                  <Form.ErrorList errors={meta.errors} />
                </div>
              )}
            </Form.List>
          )}
        </Form>
      </Modal>

      <Modal title="Assessment preview" open={Boolean(preview)} onCancel={() => setPreview(null)} footer={<Button onClick={() => setPreview(null)}>Close</Button>} width={780}>
        {preview && (
          <Space direction="vertical" size="middle" style={{ width: "100%" }}>
            <Descriptions column={{ xs: 1, sm: 2 }} items={[
              { key: "version", label: "Version", children: preview.version },
              { key: "duration", label: "Duration", children: `${preview.durationMinutes} minutes` },
              { key: "pass", label: "Pass mark", children: `${preview.passingScore}%` },
              { key: "attempts", label: "Attempt limit", children: preview.attemptLimit },
            ]} />
            <Typography.Paragraph>{preview.instructions}</Typography.Paragraph>
            {preview.questions.map((question, index) => (
              <Card key={question.id} size="small" title={`${index + 1}. ${question.prompt}`} extra={`${question.points} marks`}>
                {question.options.length ? question.options.map((option) => (
                  <Typography.Paragraph key={option.id} style={{ marginBottom: 4 }}>○ {option.text}</Typography.Paragraph>
                )) : <Typography.Text type="secondary">Written response</Typography.Text>}
              </Card>
            ))}
            {preview.questions.length === 0 && <Empty description="No questions added yet" />}
          </Space>
        )}
      </Modal>

      <Modal
        title={gradingTarget ? `Written grading — ${gradingTarget.assessmentTitle}` : "Written grading"}
        open={Boolean(gradingTarget)}
        onCancel={() => setGradingTarget(null)}
        width={850}
        footer={gradingTarget && (
          <Space wrap>
            {finalized && !released && <Button type="primary" loading={gradingBusy} onClick={() => setReleaseDialogOpen(true)}>Release result</Button>}
            {!finalized && <Button type="primary" loading={gradingBusy} onClick={() => void finalizeGrading()}>Finalize grading</Button>}
            <Button onClick={() => setGradingTarget(null)}>Close</Button>
          </Space>
        )}
      >
        {gradingLoading ? <Card loading /> : writtenGrading && gradingTarget && (
          <Space direction="vertical" style={{ width: "100%" }}>
            <Typography.Text type="secondary">
              {gradingTarget.representativeName} · Attempt {gradingTarget.attemptNumber}
              {released ? " · Result released" : finalized ? " · Finalized, not released" : ""}
            </Typography.Text>
            {writtenGrading.answers.map((answer) => (
              <Card key={answer.questionId} title={answer.prompt} extra={`Maximum ${answer.maximumMarks} marks`}>
                <Typography.Paragraph>
                  <Typography.Text strong>Response</Typography.Text><br />
                  {answer.responseText?.trim() || <Typography.Text type="secondary">No response submitted.</Typography.Text>}
                </Typography.Paragraph>
                <Alert type="info" showIcon message="Rubric" description={answer.gradingRubric || "No rubric provided."} />
                <Form
                  key={`${answer.questionId}-${answer.awardedMarks}-${answer.feedback}`}
                  layout="inline"
                  initialValues={{ awardedMarks: answer.awardedMarks ?? undefined, feedback: answer.feedback ?? "" }}
                  onFinish={(values: { awardedMarks: number; feedback?: string }) => void submitGrade(answer.questionId, values)}
                  style={{ marginTop: 16 }}
                >
                  <Form.Item name="awardedMarks" label="Awarded marks" rules={[{ required: true }]}>
                    <InputNumber min={0} max={answer.maximumMarks} precision={2} />
                  </Form.Item>
                  <Form.Item name="feedback" label="Feedback"><Input maxLength={4000} style={{ width: 280 }} /></Form.Item>
                  {!released && <Button htmlType="submit" loading={gradingBusy}>Save grade</Button>}
                </Form>
                {answer.history.length > 0 && (
                  <>
                    <Divider />
                    <Typography.Text strong>Grade history</Typography.Text>
                    {answer.history.map((grade, index) => (
                      <Typography.Paragraph key={`${grade.gradedAt}-${index}`} type="secondary">
                        {grade.awardedMarks}/{answer.maximumMarks} · {grade.feedback || "No feedback"} · {new Date(grade.gradedAt).toLocaleString()}
                      </Typography.Paragraph>
                    ))}
                  </>
                )}
              </Card>
            ))}
          </Space>
        )}
      </Modal>
      <Modal
        title="Release result?"
        open={releaseTarget !== null}
        okText="Release result"
        confirmLoading={Boolean(releaseTarget && releasingAttemptId === releaseTarget.attemptId)}
        cancelButtonProps={{ disabled: releasingAttemptId !== null }}
        closable={releasingAttemptId === null}
        maskClosable={releasingAttemptId === null}
        onCancel={() => setReleaseTarget(null)}
        onOk={async () => {
          if (releaseTarget && await releaseCompletedResult(releaseTarget)) {
            setReleaseTarget(null);
          }
        }}
      >
        {releaseTarget && `The final result for ${releaseTarget.representativeName} will become visible to the representative.`}
      </Modal>
      <Modal
        title="Release result to representative?"
        open={releaseDialogOpen}
        okText="Release result"
        confirmLoading={gradingBusy}
        cancelButtonProps={{ disabled: gradingBusy }}
        closable={!gradingBusy}
        maskClosable={!gradingBusy}
        onCancel={() => setReleaseDialogOpen(false)}
        onOk={async () => {
          if (await releaseResult()) {
            setReleaseDialogOpen(false);
          }
        }}
      >
        The representative will be able to see the final score and written feedback.
      </Modal>
    </Space>
  );
}
