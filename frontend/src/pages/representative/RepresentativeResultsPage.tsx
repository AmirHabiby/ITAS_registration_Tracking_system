import { Alert, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { portalService, type RepresentativeResult } from "../../services/portalService";

export function RepresentativeResultsPage() {
  const [results, setResults] = useState<RepresentativeResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    portalService.listMyResults()
      .then((response) => setResults(response.data))
      .catch(() => setError("Unable to load your results."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={results} rowKey="enrollmentId" columns={[
        { title: "Training", dataIndex: "trainingId" },
        { title: "Score", dataIndex: "assessmentScore" },
        { title: "Passed", render: (_, result) => <Tag color={result.passed ? "green" : "red"}>{result.passed ? "Yes" : "No"}</Tag> },
        { title: "Assessed", dataIndex: "assessedAt", render: (value: string | null) => value ? new Date(value).toLocaleString() : "Pending" },
        { title: "Status", dataIndex: "status" },
        { title: "Note", dataIndex: "assessmentNote" },
      ]} />
    </Space>
  );
}
