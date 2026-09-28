import { Alert, Button, Card, Descriptions, Modal, Space, Table, Tag, Typography, message } from "antd";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  assessmentService,
  type CandidateAssessment,
  type CandidateAttemptHistory,
} from "../../services/assessmentService";

function getErrorMessage(error: unknown, fallback: string) {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { status?: number; data?: { message?: string } } }).response;
    if (response?.status === 401 || response?.status === 403) return "Your account is not authorized to access these assessments.";
    return response?.data?.message ?? fallback;
  }
  return fallback;
}

export function RepresentativeAssessmentsPage() {
  const navigate = useNavigate();
  const [available, setAvailable] = useState<CandidateAssessment[]>([]);
  const [history, setHistory] = useState<CandidateAttemptHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [startingId, setStartingId] = useState<string | null>(null);
  const [startConfirmation, setStartConfirmation] = useState<CandidateAssessment | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    try {
      const [availableResponse, historyResponse] = await Promise.all([
        assessmentService.listEligibleAssessments(),
        assessmentService.listMyAttempts(),
      ]);
      setAvailable(availableResponse.data);
      setHistory(historyResponse.data);
      setError(null);
    } catch (loadError) {
      setError(getErrorMessage(loadError, "Unable to load assessments."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  async function start(assessment: CandidateAssessment): Promise<boolean> {
    if (assessment.activeAttemptId) {
      navigate(`/representative/assessment-attempts/${assessment.activeAttemptId}`);
      return true;
    }
    setStartingId(assessment.id);
    try {
      const attempt = await assessmentService.startAttempt(assessment.id);
      navigate(`/representative/assessment-attempts/${attempt.data.id}`);
      return true;
    } catch (startError) {
      message.error(getErrorMessage(startError, "Unable to start this assessment."));
      await load();
      return false;
    } finally {
      setStartingId(null);
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      {error && <Alert type="error" showIcon message={error} />}
      <Card title="Available assessments">
        <Table
          loading={loading}
          dataSource={available}
          rowKey="id"
          locale={{ emptyText: "No assessments are currently available for your enrolled trainings." }}
          columns={[
            { title: "Assessment", dataIndex: "title", key: "title" },
            { title: "Version", dataIndex: "version", key: "version" },
            { title: "Duration", dataIndex: "durationMinutes", key: "duration", render: (value: number) => `${value} minutes` },
            { title: "Attempts", key: "attempts", render: (_: unknown, item: CandidateAssessment) => `${item.attemptsUsed}/${item.attemptLimit}` },
            {
              title: "Action",
              key: "action",
              render: (_: unknown, assessment: CandidateAssessment) => (
                <Button
                  type="primary"
                  loading={startingId === assessment.id}
                  disabled={startingId !== null}
                  onClick={() => assessment.activeAttemptId
                    ? void start(assessment)
                    : setStartConfirmation(assessment)}
                >
                  {assessment.activeAttemptId ? "Resume attempt" : "Start assessment"}
                </Button>
              ),
            },
          ]}
          scroll={{ x: 650 }}
        />
      </Card>
      <Modal
        title="Assessment instructions"
        open={Boolean(startConfirmation)}
        closable={!startingId}
        maskClosable={!startingId}
        onCancel={() => { if (!startingId) setStartConfirmation(null); }}
        footer={[
          <Button key="cancel" disabled={Boolean(startingId)} onClick={() => setStartConfirmation(null)}>Cancel</Button>,
          <Button
            key="start"
            type="primary"
            loading={Boolean(startConfirmation && startingId === startConfirmation.id)}
            onClick={async () => {
              if (startConfirmation && await start(startConfirmation)) setStartConfirmation(null);
            }}
          >Start assessment</Button>,
        ]}
        destroyOnClose
      >
        {startConfirmation && (
          <Space direction="vertical" style={{ width: "100%" }}>
            <Typography.Paragraph style={{ whiteSpace: "pre-wrap" }}>
              {startConfirmation.instructions || "No additional instructions were provided."}
            </Typography.Paragraph>
            <Descriptions column={1} items={[
              { key: "duration", label: "Duration", children: `${startConfirmation.durationMinutes} minutes` },
              { key: "attempts", label: "Attempts used", children: `${startConfirmation.attemptsUsed} of ${startConfirmation.attemptLimit}` },
              { key: "deadline", label: "Timer", children: "Starts as soon as you begin and cannot be extended." },
            ]} />
          </Space>
        )}
      </Modal>
      <Card title="Attempt history">
        <Table
          loading={loading}
          dataSource={history}
          rowKey="attemptId"
          locale={{ emptyText: "Your completed assessment attempts will appear here." }}
          columns={[
            { title: "Assessment", dataIndex: "assessmentTitle", key: "assessment" },
            { title: "Training", dataIndex: "trainingTitle", key: "training" },
            { title: "Attempt", dataIndex: "attemptNumber", key: "attemptNumber" },
            { title: "Started", dataIndex: "startedAt", key: "started", render: (value: string) => new Date(value).toLocaleString() },
            {
              title: "Status",
              key: "status",
              render: (_: unknown, item: CandidateAttemptHistory) => {
                const color = item.status === "IN_PROGRESS" ? "blue" : item.status === "SUBMITTED" ? "gold" : item.status === "EXPIRED" ? "default" : "green";
                return <Tag color={color}>{item.status}{item.releasedResult ? " · RESULT RELEASED" : ""}</Tag>;
              },
            },
            {
              title: "Outcome",
              key: "outcome",
              render: (_: unknown, item: CandidateAttemptHistory) => item.releasedResult
                ? <Space><Typography.Text>{item.releasedResult.percentage}%</Typography.Text><Tag color={item.releasedResult.passed ? "green" : "red"}>{item.releasedResult.passed ? "Passed" : "Not passed"}</Tag></Space>
                : item.status === "IN_PROGRESS" ? (
                  <Button onClick={() => navigate(`/representative/assessment-attempts/${item.attemptId}`)}>Resume</Button>
                ) : "Pending release",
            },
          ]}
          scroll={{ x: 800 }}
        />
      </Card>
    </Space>
  );
}
