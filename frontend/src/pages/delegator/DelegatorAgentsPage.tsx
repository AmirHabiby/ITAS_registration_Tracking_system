import { Alert, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Agent } from "../../services/portalService";

export function DelegatorAgentsPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    portalService.listAgents()
      .then((response) => setAgents(response.data))
      .catch(() => setError("Unable to load delegated agents."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={agents} rowKey="delegationId" columns={[
        { title: "Representative", dataIndex: "representativeId" },
        { title: "Delegated At", dataIndex: "delegatedAt", render: (value: string) => new Date(value).toLocaleString() },
        { title: "Status", render: (_, agent) => <Tag color="green">{agent.status}</Tag> },
        { title: "Reason", dataIndex: "reason" },
      ]} />
    </Space>
  );
}
