import { Alert, Button, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Representative } from "../../services/portalService";

export function DelegatorTrainedRepresentativesPage() {
  const [representatives, setRepresentatives] = useState<Representative[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadRepresentatives() {
    try {
      setError(null);
      setRepresentatives((await portalService.listTrainedRepresentatives()).data);
    } catch {
      setError("Unable to load trained representatives.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadRepresentatives(); }, []);

  async function markAsAgent(id: string) {
    try {
      await portalService.markRepresentativeAsAgent(id, "Marked as agent");
      message.success("Representative marked as an agent.");
      await loadRepresentatives();
    } catch {
      message.error("Unable to mark representative as an agent.");
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={representatives} rowKey="id" columns={[
        { title: "Name", dataIndex: "fullName" },
        { title: "Email", dataIndex: "email" },
        { title: "Status", render: (_, representative) => <Tag>{representative.status}</Tag> },
        { title: "Action", render: (_, representative) => <Button type="primary" onClick={() => void markAsAgent(representative.id)}>Mark as agent</Button> },
      ]} />
    </Space>
  );
}
