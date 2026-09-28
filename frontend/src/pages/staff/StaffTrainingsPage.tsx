import { Alert, Button, Card, Form, Input, Layout, Space, Statistic, Typography, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type Training } from "../../services/portalService";

const { Content, Sider } = Layout;
const staffTrainingStorageKey = "rtts.staffTraining";

export function StaffTrainingsPage() {
  const [training, setTraining] = useState<Training | null>(() => {
    const storedTraining = sessionStorage.getItem(staffTrainingStorageKey);
    return storedTraining ? JSON.parse(storedTraining) as Training : null;
  });
  const [staffTrainingCount, setStaffTrainingCount] = useState(0);
  const [showMaterials, setShowMaterials] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void portalService.listStaffTrainings()
      .then((response) => setStaffTrainingCount(response.data.length))
      .catch(() => setStaffTrainingCount(0));
  }, []);

  async function accessTraining(values: { name: string; department: string; trainingPassword: string }) {
    setLoading(true);
    setError(null);
    try {
      const response = await portalService.accessStaffTraining(values);
      setTraining(response.data);
      sessionStorage.setItem(staffTrainingStorageKey, JSON.stringify(response.data));
      message.success("Training access granted.");
    } catch {
      setError("The training password is invalid or no matching staff training was found.");
    } finally {
      setLoading(false);
    }
  }

  function signOut() {
    sessionStorage.removeItem(staffTrainingStorageKey);
    setTraining(null);
    setShowMaterials(false);
  }

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider className="public-training-sidebar" width={240} theme="light" style={{ padding: 24, borderRight: "1px solid #d6d6d6" }}>
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <div>
            <Typography.Title level={4} style={{ margin: 0 }}>Staff trainings</Typography.Title>
            <Typography.Text type="secondary">Staff trainee overview</Typography.Text>
          </div>
          <Statistic title="Total staff trainings" value={staffTrainingCount} />
          <Statistic title="Selected training" value={training ? 1 : 0} />
          {training ? <Button onClick={signOut} block>Exit training</Button> : <Button type="primary" href="/login" block>Staff sign in</Button>}
        </Space>
      </Sider>
      <Layout>
        <Content style={{ padding: 32, maxWidth: 900, width: "100%", margin: "0 auto" }}>
          {!training ? (
            <Card
              style={{ width: "100%", maxWidth: 360, minHeight: 420, margin: "0 auto" }}
              title={<Typography.Title level={2} style={{ margin: 0, textAlign: "center" }}>Welcome!</Typography.Title>}
            >
              <Typography.Paragraph type="secondary">
                Enter your details and the password assigned to your staff training.
              </Typography.Paragraph>
              {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />}
              <Form layout="vertical" requiredMark={false} onFinish={(values) => void accessTraining(values)}>
                <Form.Item name="name" label="Name" rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Form.Item name="department" label="Department" rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Form.Item name="trainingPassword" label="Training password" rules={[{ required: true }]}>
                  <Input.Password />
                </Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} block>
                  {loading ? "Checking access..." : "Access training"}
                </Button>
              </Form>
            </Card>
          ) : (
            <Space direction="vertical" size="large" style={{ width: "100%" }}>
              <div>
                <Typography.Title level={2} style={{ marginBottom: 4 }}>{training.title}</Typography.Title>
                <Typography.Text type="secondary">Staff training</Typography.Text>
              </div>
              <Card>
                <Space direction="vertical" size="middle" style={{ width: "100%" }}>
                  <Typography.Paragraph style={{ margin: 0 }}>{training.description}</Typography.Paragraph>
                  <Typography.Text><strong>Start date:</strong> {training.startDate}</Typography.Text>
                  <Typography.Text><strong>End date:</strong> {training.endDate}</Typography.Text>
                  <Button type="primary" onClick={() => setShowMaterials(true)}>
                    {showMaterials ? "Materials opened" : "View training materials"}
                  </Button>
                </Space>
              </Card>
              {showMaterials && (
                <Alert message="No materials uploaded" description="Course materials for this staff training are not available yet." type="info" showIcon />
              )}
            </Space>
          )}
        </Content>
      </Layout>
    </Layout>
  );
}
