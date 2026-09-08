import { Alert, Button, Card, Form, Input, InputNumber, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Training } from "../../services/portalService";

export function InstituteTrainingsPage() {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadTrainings() {
    try {
      setError(null);
      setTrainings((await portalService.listInstituteTrainings()).data);
    } catch {
      setError("Unable to load institute trainings.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadTrainings(); }, []);

  async function createTraining(values: { instituteId: string; title: string; description: string; startDate: string; endDate: string; capacity: number }) {
    setSubmitting(true);
    try {
      await portalService.createInstituteTraining(values);
      message.success("Training created.");
      await loadTrainings();
    } catch {
      message.error("Unable to create training. Check that the institute ID belongs to your account.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Card title="Create training">
        <Form layout="vertical" onFinish={(values) => void createTraining(values)}>
          <Form.Item name="instituteId" label="Institute profile ID" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="title" label="Title" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="description" label="Description" rules={[{ required: true }]}><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="startDate" label="Start date" rules={[{ required: true }]}><Input type="date" /></Form.Item>
          <Form.Item name="endDate" label="End date" rules={[{ required: true }]}><Input type="date" /></Form.Item>
          <Form.Item name="capacity" label="Capacity" rules={[{ required: true }]}><InputNumber min={1} style={{ width: "100%" }} /></Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>Create training</Button>
        </Form>
      </Card>
      <Table loading={loading} dataSource={trainings} rowKey="id" columns={[
        { title: "Title", dataIndex: "title" },
        { title: "Dates", render: (_, training) => `${training.startDate} - ${training.endDate}` },
        { title: "Capacity", dataIndex: "capacity" },
        { title: "Status", render: (_, training) => <Tag color={training.active ? "green" : "default"}>{training.status}</Tag> },
      ]} />
    </Space>
  );
}
