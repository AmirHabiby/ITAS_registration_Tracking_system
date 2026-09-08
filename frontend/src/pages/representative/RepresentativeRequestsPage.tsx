import { Alert, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { portalService, type TrainingRequest } from "../../services/portalService";

export function RepresentativeRequestsPage() {
  const [requests, setRequests] = useState<TrainingRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    portalService.listMyRequests()
      .then((response) => setRequests(response.data))
      .catch(() => setError("Unable to load your training requests."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={requests} rowKey="id" columns={[
        { title: "Training", dataIndex: "trainingId" },
        { title: "Requested", dataIndex: "requestedAt", render: (value: string) => new Date(value).toLocaleString() },
        { title: "Status", render: (_, request) => <Tag>{request.status}</Tag> },
        { title: "Reviewer", dataIndex: "reviewerUsername" },
        { title: "Note", dataIndex: "reviewerNote" },
      ]} />
    </Space>
  );
}
