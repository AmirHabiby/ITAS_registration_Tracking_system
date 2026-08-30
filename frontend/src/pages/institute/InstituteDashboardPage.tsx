import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";

type Dashboard = {
  totalTrainings: number;
  enrolledRepresentatives: number;
  completedTrainings: number;
  failedTrainees: number;
  retakeRequiredTrainees: number;
};

export function InstituteDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/dashboard/institute")
      .then((response) => setData(response.data));
  }, []);

  const dashboard =
    data ??
    ({
      totalTrainings: 0,
      enrolledRepresentatives: 0,
      completedTrainings: 0,
      failedTrainees: 0,
      retakeRequiredTrainees: 0,
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
