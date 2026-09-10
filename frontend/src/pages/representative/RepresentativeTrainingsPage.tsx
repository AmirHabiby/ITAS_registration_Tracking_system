import { Alert, Button, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { useAuth } from "../../hooks/useAuth";
import { portalService, type Training } from "../../services/portalService";

export function RepresentativeTrainingsPage() {
  const { user } = useAuth();
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadTrainings() {
    try {
      setError(null);
      setTrainings((await portalService.listAvailableTrainings()).data);
    } catch {
      setError("Unable to load available trainings.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadTrainings(); }, []);

  async function requestTraining(trainingId: string) {
    if (!user?.id) return;
    try {
      await portalService.requestTraining(trainingId, user.id);
      message.success("Training request submitted.");
    } catch {
      message.error("Unable to submit the training request.");
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={trainings} rowKey="id" columns={[
        { title: "Title", dataIndex: "title" },
        { title: "Description", dataIndex: "description" },
        { title: "Dates", render: (_, training) => `${training.startDate} - ${training.endDate}` },
        { title: "Capacity", dataIndex: "capacity" },
        { title: "Access", dataIndex: "accessType", render: (accessType: Training["accessType"]) => <Tag>{accessType}</Tag> },
        { title: "Status", render: (_, training) => <Tag color={training.active ? "green" : "default"}>{training.status}</Tag> },
        { title: "Action", render: (_, training) => <Button type="primary" onClick={() => void requestTraining(training.id)} disabled={!training.active}>Request</Button> },
      ]} />
    </Space>
  );
}
