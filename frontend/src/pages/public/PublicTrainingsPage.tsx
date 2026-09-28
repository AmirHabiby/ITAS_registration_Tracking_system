import { Alert, Button, Card, Col, Collapse, Layout, List, Row, Space, Statistic, Tag, Typography, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type PublicTrainingSummary, type Training, type TrainingMaterial } from "../../services/portalService";
import axios from "axios";

const { Content, Sider } = Layout;

const emptySummary: PublicTrainingSummary = {
  totalTrainings: 0,
  enrolledTrainings: 0,
  remainingTrainings: 0,
};

export function PublicTrainingsPage() {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [summary, setSummary] = useState<PublicTrainingSummary>(emptySummary);
  const [loading, setLoading] = useState(true);
  const [enrollingId, setEnrollingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [enrolledTrainingIds, setEnrolledTrainingIds] = useState<Set<string>>(new Set());
  const [materials, setMaterials] = useState<Record<string, TrainingMaterial[]>>({});
  const [materialsLoading, setMaterialsLoading] = useState(true);

  async function loadPublicTrainings() {
    try {
      setError(null);
      const [trainingResponse, summaryResponse] = await Promise.all([
        portalService.listPublicTrainings(),
        portalService.getPublicTrainingSummary(),
      ]);
      const publicTrainings = trainingResponse.data.filter((training) => training.accessType === "PUBLIC");
      setTrainings(publicTrainings);
      setSummary(summaryResponse.data);
      const enrollmentResponse = await portalService.listPublicEnrollments();
      const enrolledIds = new Set(enrollmentResponse.data.map((enrollment) => enrollment.trainingId));
      setEnrolledTrainingIds(enrolledIds);
      const materialEntries = await Promise.all(
        [...enrolledIds].map(async (trainingId) => {
          const response = await portalService.listPublicTrainingMaterials(trainingId);
          return [trainingId, response.data] as const;
        }),
      );
      setMaterials(Object.fromEntries(materialEntries));
    } catch {
      setError("Unable to load public trainings.");
    } finally {
      setLoading(false);
      setMaterialsLoading(false);
    }
  }

  useEffect(() => {
    void loadPublicTrainings();
  }, []);

  async function enroll(trainingId: string) {
    setEnrollingId(trainingId);
    try {
      await portalService.enrollPublicTrainee(trainingId);
      setEnrolledTrainingIds((current) => new Set(current).add(trainingId));
      setMaterials((current) => ({ ...current, [trainingId]: [] }));
      const summaryResponse = await portalService.getPublicTrainingSummary();
      setSummary(summaryResponse.data);
      message.success("Enrollment submitted.");
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 409) {
        setEnrolledTrainingIds((current) => new Set(current).add(trainingId));
        setMaterials((current) => ({ ...current, [trainingId]: [] }));
        message.info("You are already enrolled in this training.");
      } else {
        message.error("Unable to enroll in this training.");
      }
    } finally {
      setEnrollingId(null);
    }
  }

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider className="public-training-sidebar" width={240} theme="light" style={{ padding: 24, borderRight: "1px solid #d6d6d6" }}>
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <div>
            <Typography.Title level={4} style={{ margin: 0 }}>Public trainings</Typography.Title>
            <Typography.Text type="secondary">Trainee overview</Typography.Text>
          </div>
          <Statistic title="Total training" value={summary.totalTrainings} />
          <Statistic title="Enrolled trainings" value={summary.enrolledTrainings} />
          <Statistic title="Remaining trainings" value={summary.remainingTrainings} />
          <Button type="primary" href="/login" block>
            Sign in
          </Button>
        </Space>
      </Sider>
      <Layout>
        <Content style={{ padding: 32, maxWidth: 1100, width: "100%", margin: "0 auto" }}>
          <Space direction="vertical" size="large" style={{ width: "100%" }}>
            <div>
              <Typography.Title level={2} style={{ marginBottom: 4 }}>Available public trainings</Typography.Title>
              <Typography.Text type="secondary">Choose a training and enroll online.</Typography.Text>
            </div>
            {error && <Alert message={error} type="error" showIcon />}
            <List
              loading={loading}
              dataSource={trainings}
              locale={{ emptyText: "No public trainings are currently available." }}
              renderItem={(training) => {
                const enrolled = enrolledTrainingIds.has(training.id);
                return (
                  <List.Item style={{ padding: 0, marginBottom: 16 }}>
                    <Card
                      style={{ width: "100%" }}
                      title={<Typography.Title level={4} style={{ margin: 0 }}>{training.title}</Typography.Title>}
                      extra={
                        <Button
                          type="primary"
                          loading={enrollingId === training.id}
                          disabled={enrolled || training.capacity <= 0}
                          onClick={() => void enroll(training.id)}
                        >
                          {enrollingId === training.id ? "Enrolling..." : enrolled ? "Enrolled" : training.capacity <= 0 ? "Fully booked" : "Enroll in training"}
                        </Button>
                      }
                    >
                      <Space direction="vertical" size="middle" style={{ width: "100%" }}>
                        <Typography.Paragraph style={{ margin: 0 }}>{training.description}</Typography.Paragraph>
                        <Row gutter={[24, 12]}>
                          <Col xs={24} sm={8}><Typography.Text type="secondary">Start date</Typography.Text><br />{training.startDate}</Col>
                          <Col xs={24} sm={8}><Typography.Text type="secondary">End date</Typography.Text><br />{training.endDate}</Col>
                          <Col xs={24} sm={8}><Typography.Text type="secondary">Capacity</Typography.Text><br /><Tag>{training.capacity}</Tag></Col>
                        </Row>
                        {enrolled && (
                          <Card size="small" title="Course materials" loading={materialsLoading}>
                            {(materials[training.id] ?? []).length === 0 ? (
                              <Typography.Text type="secondary">No materials have been uploaded yet.</Typography.Text>
                            ) : (
                              <Collapse
                                items={Array.from(new Set((materials[training.id] ?? []).map((material) => material.weekNumber)))
                                  .sort((a, b) => a - b)
                                  .map((weekNumber) => ({
                                    key: weekNumber,
                                    label: `Week ${weekNumber}`,
                                    children: (
                                      <List
                                        size="small"
                                        dataSource={(materials[training.id] ?? []).filter((material) => material.weekNumber === weekNumber)}
                                        renderItem={(material) => (
                                          <List.Item actions={[<Button type="link" href={material.fileUrl} target="_blank" rel="noreferrer">Review material</Button>]}>
                                            <List.Item.Meta title={material.title} description={material.description ?? material.materialType} />
                                          </List.Item>
                                        )}
                                      />
                                    ),
                                  }))}
                              />
                            )}
                          </Card>
                        )}
                      </Space>
                    </Card>
                  </List.Item>
                );
              }}
            />
          </Space>
        </Content>
      </Layout>
    </Layout>
  );
}
