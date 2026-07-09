import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";

type Dashboard = {
  pendingTrainingRequests: number;
  approvedTrainingRequests: number;
  rejectedTrainingRequests: number;
  trainedRepresentatives: number;
  delegatedAgents: number;
};

export function DelegatorDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/delegator/dashboard")
      .then((response) => setData(response.data));
  }, []);

  const dashboard =
    data ??
    ({
      pendingTrainingRequests: 0,
      approvedTrainingRequests: 0,
      rejectedTrainingRequests: 0,
      trainedRepresentatives: 0,
      delegatedAgents: 0,
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
