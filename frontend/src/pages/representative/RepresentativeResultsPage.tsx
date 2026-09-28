import { Alert, Button, Card, Descriptions, Modal, Space, Table, Tag, Typography, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type RepresentativeResult } from "../../services/portalService";
import {
  assessmentService,
  type AssessmentResult,
  type CandidateAttemptHistory,
} from "../../services/assessmentService";

export function RepresentativeResultsPage() {
  const [results, setResults] = useState<RepresentativeResult[]>([]);
  const [onlineResults, setOnlineResults] = useState<CandidateAttemptHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resultDetail, setResultDetail] = useState<AssessmentResult | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    let active = true;
    Promise.allSettled([portalService.listMyResults(), assessmentService.listMyAttempts()])
      .then(([legacyResponse, onlineResponse]) => {
        if (!active) return;
        if (legacyResponse.status === "fulfilled") setResults(legacyResponse.value.data);
        if (onlineResponse.status === "fulfilled") setOnlineResults(onlineResponse.value.data);
        if (legacyResponse.status === "rejected" || onlineResponse.status === "rejected") {
          setError("Some result data could not be loaded. Refresh the page to retry.");
        }
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function showResult(attemptId: string) {
    setDetailLoading(true);
    try {
      setResultDetail((await assessmentService.getReleasedResult(attemptId)).data);
    } catch {
      message.error("This assessment result is not available or has not been released.");
    } finally {
      setDetailLoading(false);
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Card title="Online assessment results">
        <Table
          loading={loading}
          dataSource={onlineResults}
          rowKey="attemptId"
          locale={{ emptyText: "Released online assessment results will appear here." }}
          columns={[
            { title: "Assessment", dataIndex: "assessmentTitle", key: "assessment" },
            { title: "Training", dataIndex: "trainingTitle", key: "training" },
            { title: "Attempt", dataIndex: "attemptNumber", key: "attempt" },
            { title: "Status", dataIndex: "status", key: "status", render: (status: string) => <Tag>{status}</Tag> },
            {
              title: "Result",
              key: "result",
              render: (_: unknown, item: CandidateAttemptHistory) => item.releasedResult
                ? <Space><span>{item.releasedResult.percentage}%</span><Tag color={item.releasedResult.passed ? "green" : "red"}>{item.releasedResult.passed ? "Passed" : "Not passed"}</Tag></Space>
                : "Not released",
            },
            {
              title: "Feedback",
              key: "feedback",
              render: (_: unknown, item: CandidateAttemptHistory) => item.releasedResult
                ? <Button onClick={() => void showResult(item.attemptId)}>View result and feedback</Button>
                : "—",
            },
          ]}
          scroll={{ x: 700 }}
        />
      </Card>
      <Card title="Training assessment results">
        <Table
          loading={loading}
          dataSource={results}
          rowKey="enrollmentId"
          locale={{ emptyText: "No training assessment results available." }}
          columns={[
            { title: "Training", dataIndex: "trainingId" },
            { title: "Score", dataIndex: "assessmentScore" },
            { title: "Passed", render: (_, result) => <Tag color={result.passed ? "green" : "red"}>{result.passed ? "Yes" : "No"}</Tag> },
            { title: "Assessed", dataIndex: "assessedAt", render: (value: string | null) => value ? new Date(value).toLocaleString() : "Pending" },
            { title: "Status", dataIndex: "status" },
            { title: "Note", dataIndex: "assessmentNote" },
          ]}
          scroll={{ x: 650 }}
        />
      </Card>
      <Modal
        title="Released assessment result"
        open={Boolean(resultDetail) || detailLoading}
        onCancel={() => setResultDetail(null)}
        footer={<Button onClick={() => setResultDetail(null)}>Close</Button>}
        confirmLoading={detailLoading}
      >
        {resultDetail && (
          <Space direction="vertical" style={{ width: "100%" }}>
            <Descriptions column={1} items={[
              { key: "marks", label: "Score", children: `${resultDetail.awardedMarks} / ${resultDetail.maximumMarks}` },
              { key: "percentage", label: "Percentage", children: `${resultDetail.percentage}%` },
              { key: "outcome", label: "Outcome", children: <Tag color={resultDetail.passed ? "green" : "red"}>{resultDetail.passed ? "Passed" : "Not passed"}</Tag> },
              { key: "released", label: "Released", children: resultDetail.releasedAt ? new Date(resultDetail.releasedAt).toLocaleString() : "—" },
            ]} />
            <Typography.Title level={5}>Written question feedback</Typography.Title>
            {resultDetail.writtenFeedback.length
              ? resultDetail.writtenFeedback.map((feedback) => (
                <Card key={feedback.questionId} size="small" title={feedback.prompt} extra={`${feedback.awardedMarks}/${feedback.maximumMarks}`}>
                  {feedback.feedback || <Typography.Text type="secondary">No feedback provided.</Typography.Text>}
                </Card>
              ))
              : <Typography.Text type="secondary">No written-question feedback.</Typography.Text>}
          </Space>
        )}
      </Modal>
    </Space>
  );
}
