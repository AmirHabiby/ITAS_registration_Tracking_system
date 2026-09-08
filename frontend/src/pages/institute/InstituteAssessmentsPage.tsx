import { Alert, Button, Card, Form, Input, InputNumber, Space, Table, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Assessment } from "../../services/portalService";

export function InstituteAssessmentsPage() {
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadAssessments() {
    try {
      setError(null);
      setAssessments((await portalService.listAssessments()).data);
    } catch {
      setError("Unable to load assessments.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadAssessments(); }, []);

  async function submit(values: { enrollmentId: string; score: number; remarks: string; assessmentDate: string }) {
    setSubmitting(true);
    try {
      await portalService.submitAssessment({ ...values, assessmentDate: new Date(values.assessmentDate).toISOString() });
      message.success("Assessment submitted.");
      await loadAssessments();
    } catch {
      message.error("Unable to submit assessment.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Card title="Submit assessment">
        <Form layout="vertical" onFinish={(values) => void submit(values)}>
          <Form.Item name="enrollmentId" label="Enrollment ID" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="score" label="Score" rules={[{ required: true }]}><InputNumber min={0} style={{ width: "100%" }} /></Form.Item>
          <Form.Item name="remarks" label="Remarks"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="assessmentDate" label="Assessment date and time" rules={[{ required: true }]}><Input type="datetime-local" /></Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>Submit assessment</Button>
        </Form>
      </Card>
      <Table loading={loading} dataSource={assessments} rowKey="id" columns={[
        { title: "Enrollment", dataIndex: "trainingEnrollmentId" },
        { title: "Score", dataIndex: "score" },
        { title: "Passed", dataIndex: "passed", render: (value: boolean) => value ? "Yes" : "No" },
        { title: "Date", dataIndex: "assessmentDate", render: (value: string) => new Date(value).toLocaleString() },
        { title: "Remarks", dataIndex: "remarks" },
      ]} />
    </Space>
  );
}
