import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";

type Dashboard = {
  availableTrainings: number;
  myPendingRequests: number;
  myApprovedTrainings: number;
  myCompletedTrainings: number;
  currentRepresentativeStatus: string;
};

export function RepresentativeDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/representative/dashboard")
      .then((response) => setData(response.data));
  }, []);

  const dashboard =
    data ??
    ({
      availableTrainings: 0,
      myPendingRequests: 0,
      myApprovedTrainings: 0,
      myCompletedTrainings: 0,
      currentRepresentativeStatus: "REGISTERED",
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
