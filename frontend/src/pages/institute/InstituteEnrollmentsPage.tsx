import { Alert, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Enrollment } from "../../services/portalService";

export function InstituteEnrollmentsPage() {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    portalService.listInstituteEnrollments()
      .then((response) => setEnrollments(response.data))
      .catch(() => setError("Unable to load enrollments."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={enrollments} rowKey="id" columns={[
        { title: "Representative", dataIndex: "representativeId" },
        { title: "Training", dataIndex: "trainingId" },
        { title: "Score", dataIndex: "assessmentScore" },
        { title: "Passed", render: (_, enrollment) => enrollment.passed === null ? "Pending" : <Tag color={enrollment.passed ? "green" : "red"}>{enrollment.passed ? "Yes" : "No"}</Tag> },
        { title: "Status", dataIndex: "status" },
        { title: "Assessed", dataIndex: "assessedAt", render: (value: string | null) => value ? new Date(value).toLocaleString() : "Pending" },
      ]} />
    </Space>
  );
}
