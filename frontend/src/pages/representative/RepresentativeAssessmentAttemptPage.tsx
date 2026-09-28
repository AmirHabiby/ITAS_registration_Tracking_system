import {
  Alert,
  Button,
  Card,
  Input,
  Modal,
  Progress,
  Radio,
  Space,
  Tag,
  Typography,
  message,
} from "antd";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  assessmentService,
  type CandidateAttempt,
  type CandidateAttemptQuestion,
} from "../../services/assessmentService";
import { formatCountdown } from "./assessmentUtils";

type Answer = { selectedOptionId: string | null; responseText: string | null };
type SaveStatus = "saved" | "unsaved" | "saving" | "error";

function getErrorMessage(error: unknown, fallback: string) {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { status?: number; data?: { message?: string; detail?: string } } }).response;
    if (response?.status === 401 || response?.status === 403) return "Your account is not authorized to access this attempt.";
    if (response?.status === 404) return "This attempt could not be found.";
    if (response?.status === 409) return response.data?.message ?? "This attempt is no longer active.";
    return response?.data?.message ?? response?.data?.detail ?? fallback;
  }
  return fallback;
}

export function RepresentativeAssessmentAttemptPage() {
  const { attemptId } = useParams();
  const navigate = useNavigate();
  const [attempt, setAttempt] = useState<CandidateAttempt | null>(null);
  const [answers, setAnswers] = useState<Record<string, Answer>>({});
  const answersRef = useRef<Record<string, Answer>>({});
  const dirtyQuestions = useRef(new Set<string>());
  const saveChains = useRef(new Map<string, Promise<void>>());
  const timers = useRef(new Map<string, ReturnType<typeof setTimeout>>());
  const expirySyncing = useRef(false);
  const syncAt = useRef(Date.now());
  const secondsAtSync = useRef(0);
  const [now, setNow] = useState(Date.now());
  const [saveStatuses, setSaveStatuses] = useState<Record<string, SaveStatus>>({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const submittingRef = useRef(false);
  const [submitModalOpen, setSubmitModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const syncTimer = useCallback((updated: CandidateAttempt) => {
    syncAt.current = Date.now();
    secondsAtSync.current = updated.remainingSeconds;
    setAttempt((current) => current ? {
      ...current,
      status: updated.status,
      deadlineAt: updated.deadlineAt,
      remainingSeconds: updated.remainingSeconds,
      submittedAt: updated.submittedAt,
      expiredAt: updated.expiredAt,
    } : updated);
  }, []);

  const loadAttempt = useCallback(async () => {
    if (!attemptId) return;
    try {
      const response = await assessmentService.resumeAttempt(attemptId);
      const current = response.data;
      setAttempt(current);
      const initial = Object.fromEntries(current.questions.map((question) => [
        question.id,
        { selectedOptionId: question.selectedOptionId, responseText: question.responseText },
      ]));
      answersRef.current = initial;
      setAnswers(initial);
      syncAt.current = Date.now();
      secondsAtSync.current = current.remainingSeconds;
      setError(null);
    } catch (loadError) {
      setError(getErrorMessage(loadError, "Unable to load the assessment attempt."));
    } finally {
      setLoading(false);
    }
  }, [attemptId]);

  useEffect(() => { void loadAttempt(); }, [loadAttempt]);

  const persistAnswer = useCallback((questionId: string) => {
    if (!attemptId) return Promise.resolve();
    dirtyQuestions.current.delete(questionId);
    const previous = saveChains.current.get(questionId) ?? Promise.resolve();
    const currentSave = previous.catch(() => undefined).then(async () => {
      const answer = answersRef.current[questionId];
      if (!answer) return;
      setSaveStatuses((current) => ({ ...current, [questionId]: "saving" }));
      try {
        const result = await assessmentService.saveAnswer(attemptId, questionId, answer);
        syncTimer(result.data);
        setSaveStatuses((current) => ({ ...current, [questionId]: "saved" }));
      } catch (saveError) {
        dirtyQuestions.current.add(questionId);
        setSaveStatuses((current) => ({ ...current, [questionId]: "error" }));
        setError(getErrorMessage(saveError, "Some answers could not be autosaved. Try again before submitting."));
        throw saveError;
      }
    }).finally(() => {
      if (saveChains.current.get(questionId) === currentSave) saveChains.current.delete(questionId);
    });
    saveChains.current.set(questionId, currentSave);
    return currentSave;
  }, [attemptId, syncTimer]);

  function queueSave(questionId: string, answer: Answer) {
    answersRef.current = { ...answersRef.current, [questionId]: answer };
    setAnswers(answersRef.current);
    dirtyQuestions.current.add(questionId);
    setSaveStatuses((current) => ({ ...current, [questionId]: "unsaved" }));
    const existing = timers.current.get(questionId);
    if (existing) clearTimeout(existing);
    timers.current.set(questionId, setTimeout(() => {
      timers.current.delete(questionId);
      void persistAnswer(questionId).catch(() => undefined);
    }, 650));
  }

  const flushPendingSaves = useCallback(async () => {
    for (const timer of timers.current.values()) clearTimeout(timer);
    timers.current.clear();
    const questionIds = [...dirtyQuestions.current];
    const results = await Promise.allSettled([
      ...questionIds.map((id) => persistAnswer(id)),
      ...saveChains.current.values(),
    ]);
    const failed = results.find((result): result is PromiseRejectedResult => result.status === "rejected");
    if (failed) throw failed.reason;
  }, [persistAnswer]);

  useEffect(() => {
    const interval = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(interval);
  }, []);

  useEffect(() => {
    if (attempt?.status !== "IN_PROGRESS") return;
    const warnBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = "";
    };
    window.addEventListener("beforeunload", warnBeforeUnload);
    return () => window.removeEventListener("beforeunload", warnBeforeUnload);
  }, [attempt?.status]);

  useEffect(() => () => {
    for (const timer of timers.current.values()) clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (!attempt || attempt.status !== "IN_PROGRESS") return;
    const remaining = Math.max(0, secondsAtSync.current - Math.floor((now - syncAt.current) / 1000));
    if (remaining === 0 && !expirySyncing.current) {
      expirySyncing.current = true;
      void loadAttempt().finally(() => { expirySyncing.current = false; });
    }
  }, [attempt?.status, now, loadAttempt]);

  async function submitAttempt() {
    if (!attemptId || submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    try {
      await flushPendingSaves();
      const result = await assessmentService.submitAttempt(attemptId);
      syncTimer(result.data);
      setAttempt(result.data);
      setSubmitModalOpen(false);
      message.success(result.data.status === "EXPIRED" ? "The deadline passed. Your attempt is closed." : "Assessment submitted.");
    } catch (submitError) {
      setError(getErrorMessage(submitError, "Unable to submit. Your saved answers remain available; please retry."));
      try {
        const latest = await assessmentService.resumeAttempt(attemptId);
        syncTimer(latest.data);
        if (latest.data.status !== "IN_PROGRESS") setAttempt(latest.data);
      } catch {
        setError(getErrorMessage(submitError, "Unable to confirm attempt status. Refresh to safely resume."));
      }
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  }

  if (loading) return <Card loading />;
  if (!attempt) return <Alert type="error" showIcon message={error ?? "Attempt unavailable."} action={<Button onClick={() => navigate("/representative/assessments")}>Back to assessments</Button>} />;

  const current = [...attempt.questions].sort((a, b) => a.displayOrder - b.displayOrder)[currentIndex];
  const remaining = Math.max(0, secondsAtSync.current - Math.floor((now - syncAt.current) / 1000));
  const answeredCount = attempt.questions.filter((question) => {
    const answer = answers[question.id];
    return question.questionType === "MULTIPLE_CHOICE"
      ? Boolean(answer?.selectedOptionId)
      : Boolean(answer?.responseText?.trim());
  }).length;
  const isActive = attempt.status === "IN_PROGRESS" && remaining > 0;

  if (attempt.status !== "IN_PROGRESS") {
    return (
      <Card title={attempt.assessmentTitle}>
        <Alert
          showIcon
          type={attempt.status === "EXPIRED" ? "warning" : "success"}
          message={attempt.status === "EXPIRED" ? "Time expired" : "Attempt submitted"}
          description={attempt.status === "EXPIRED"
            ? "The server closed this attempt at the deadline. Unanswered questions were left blank."
            : "Your answers have been submitted and are immutable. The result will appear here after it is released."}
        />
        {error && <Alert style={{ marginTop: 16 }} type="error" showIcon message={error} />}
        <Button style={{ marginTop: 16 }} onClick={() => navigate("/representative/assessments")}>View attempt history</Button>
      </Card>
    );
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert type="error" showIcon message={error} closable onClose={() => setError(null)} />}
      <Card
        title={<Space wrap><span>{attempt.assessmentTitle}</span><Tag color={remaining < 300 ? "red" : "blue"}>{formatCountdown(remaining)}</Tag></Space>}
        extra={<Typography.Text>{answeredCount}/{attempt.questions.length} answered</Typography.Text>}
      >
        <Typography.Paragraph type="secondary">
          Attempt {attempt.assessmentVersion} · Started {new Date(attempt.startedAt).toLocaleString()} · Deadline {new Date(attempt.deadlineAt).toLocaleString()}
        </Typography.Paragraph>
        <Progress percent={attempt.questions.length ? Math.round(answeredCount * 100 / attempt.questions.length) : 0} showInfo={false} />
        {!isActive && <Alert style={{ marginTop: 16 }} type="warning" showIcon message="Time has expired. Refreshing the server attempt status…" />}
      </Card>

      <div className="assessment-exam-layout">
        <Card title="Questions" className="assessment-question-nav">
          <Space wrap>
            {[...attempt.questions].sort((a, b) => a.displayOrder - b.displayOrder).map((question, index) => {
              const answer = answers[question.id];
              const answered = question.questionType === "MULTIPLE_CHOICE"
                ? Boolean(answer?.selectedOptionId)
                : Boolean(answer?.responseText?.trim());
              return (
                <Button
                  key={question.id}
                  type={index === currentIndex ? "primary" : "default"}
                  onClick={() => setCurrentIndex(index)}
                  aria-label={`Question ${index + 1}${answered ? ", answered" : ", unanswered"}`}
                >
                  {index + 1}{answered ? " ✓" : ""}
                </Button>
              );
            })}
          </Space>
        </Card>
        {current ? (
          <Card
            title={`Question ${currentIndex + 1} of ${attempt.questions.length}`}
            extra={<Tag>{current.points} marks</Tag>}
          >
            <Typography.Paragraph style={{ whiteSpace: "pre-wrap" }}>{current.prompt}</Typography.Paragraph>
            {current.questionType === "MULTIPLE_CHOICE" ? (
              <Radio.Group
                value={answers[current.id]?.selectedOptionId ?? undefined}
                disabled={!isActive}
                onChange={(event) => queueSave(current.id, {
                  selectedOptionId: event.target.value,
                  responseText: null,
                })}
              >
                <Space direction="vertical">
                  {current.options.map((option) => <Radio key={option.id} value={option.id}>{option.text}</Radio>)}
                </Space>
              </Radio.Group>
            ) : (
              <Input.TextArea
                rows={8}
                maxLength={4000}
                showCount
                disabled={!isActive}
                value={answers[current.id]?.responseText ?? ""}
                onChange={(event) => queueSave(current.id, {
                  selectedOptionId: null,
                  responseText: event.target.value,
                })}
                placeholder="Enter your response"
              />
            )}
            <Typography.Text type="secondary" style={{ display: "block", marginTop: 16 }} aria-live="polite">
              {saveStatuses[current.id] === "saving" ? "Saving…" : saveStatuses[current.id] === "unsaved" ? "Unsaved changes" : saveStatuses[current.id] === "error" ? "Save failed — retry by changing the answer" : saveStatuses[current.id] === "saved" ? "All changes saved" : ""}
            </Typography.Text>
            <Space style={{ marginTop: 20 }}>
              <Button disabled={currentIndex === 0} onClick={() => setCurrentIndex((index) => Math.max(0, index - 1))}>Previous</Button>
              <Button disabled={currentIndex >= attempt.questions.length - 1} onClick={() => setCurrentIndex((index) => Math.min(attempt.questions.length - 1, index + 1))}>Next</Button>
            </Space>
          </Card>
        ) : <Card>No questions were included in this attempt.</Card>}
      </div>
      <Card>
        <Space wrap>
          <Typography.Text type="secondary">{answeredCount} of {attempt.questions.length} questions answered.</Typography.Text>
          <Button
            type="primary"
            danger
            disabled={!isActive}
            loading={submitting}
            onClick={() => {
              setError(null);
              setSubmitModalOpen(true);
            }}
          >
            Review and submit
          </Button>
        </Space>
      </Card>
      <Modal
        title="Review and submit?"
        open={submitModalOpen}
        okText="Submit attempt"
        okButtonProps={{ danger: true }}
        confirmLoading={submitting}
        cancelButtonProps={{ disabled: submitting }}
        closable={!submitting}
        maskClosable={!submitting}
        onCancel={() => setSubmitModalOpen(false)}
        onOk={submitAttempt}
      >
        {error && <Alert style={{ marginBottom: 16 }} type="error" showIcon message={error} />}
        {attempt.questions.length - answeredCount} question(s) are unanswered. Submitted answers cannot be changed.
      </Modal>
    </Space>
  );
}
