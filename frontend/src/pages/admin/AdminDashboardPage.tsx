import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";

type Dashboard = {
  totalRepresentatives: number;
  totalDelegators: number;
  totalTrainingInstitutes: number;
  totalTrainings: number;
  totalTrainedRepresentatives: number;
  totalAgents: number;
  pendingRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
};

export function AdminDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/dashboard/admin")
      .then((response) => setData(response.data));
  }, []);

  const dashboard =
    data ??
    ({
      totalRepresentatives: 0,
      totalDelegators: 0,
      totalTrainingInstitutes: 0,
      totalTrainings: 0,
      totalTrainedRepresentatives: 0,
      totalAgents: 0,
      pendingRequests: 0,
      approvedRequests: 0,
      rejectedRequests: 0,
    } satisfies Dashboard);

  return (
    <Row gutter={16}>
      {Object.entries(dashboard).map(([key, value]) => (
        <Col span={8} key={key}>
          <Card>
            <Statistic title={key} value={value} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}
