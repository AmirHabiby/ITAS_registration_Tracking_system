import { Alert, Card, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { assessmentService, type DelegatorAssessmentOutcome } from "../../services/assessmentService";

export function DelegatorAssessmentOutcomes() {
  const [outcomes, setOutcomes] = useState<DelegatorAssessmentOutcome[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    assessmentService.listDelegatorOutcomes()
      .then((response) => setOutcomes(response.data))
      .catch(() => setError("Unable to load released outcomes for your assigned representatives."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Card title="Released assessment outcomes">
      {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />}
      <Table
        loading={loading}
        dataSource={outcomes}
        rowKey="attemptId"
        locale={{ emptyText: "Released outcomes for your active representative assignments will appear here." }}
        columns={[
          { title: "Representative", dataIndex: "representativeName", key: "representative" },
          { title: "Training", dataIndex: "trainingTitle", key: "training" },
          { title: "Assessment", dataIndex: "assessmentTitle", key: "assessment" },
          { title: "Attempt", dataIndex: "attemptNumber", key: "attempt" },
          { title: "Score", key: "score", render: (_: unknown, outcome: DelegatorAssessmentOutcome) => `${outcome.awardedMarks}/${outcome.maximumMarks} (${outcome.percentage}%)` },
          { title: "Outcome", key: "outcome", render: (_: unknown, outcome: DelegatorAssessmentOutcome) => <Tag color={outcome.passed ? "green" : "red"}>{outcome.passed ? "Passed" : "Not passed"}</Tag> },
          { title: "Released", dataIndex: "releasedAt", key: "released", render: (value: string) => new Date(value).toLocaleString() },
        ]}
        pagination={{ pageSize: 8 }}
        scroll={{ x: 900 }}
      />
    </Card>
  );
}
