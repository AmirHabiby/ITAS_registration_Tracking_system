import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";

type Dashboard = {
  availableTrainings: number;
  myPendingRequests: number;
  myApprovedTrainings: number;
  myCompletedTrainings: number;
  currentRepresentativeStatus: string;
  firmName: string | null;
};

export function RepresentativeDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/representatives/me/dashboard")
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
      firmName: null,
    } satisfies Dashboard);

  const statistics = [
    ["Firm", dashboard.firmName ?? "No firm assigned"],
    ["Available trainings", dashboard.availableTrainings],
    ["My pending requests", dashboard.myPendingRequests],
    ["My approved trainings", dashboard.myApprovedTrainings],
    ["My completed trainings", dashboard.myCompletedTrainings],
    ["Current representative status", dashboard.currentRepresentativeStatus],
  ] as const;

  return (
    <Row gutter={16} className="dashboard-cards-row">
      {statistics.map(([key, value]) => (
        <Col span={8} key={key}>
          <Card>
            <Statistic
              title={key}
              value={value ?? "-"}
              valueStyle={key === "Firm" ? { fontSize: 20 } : undefined}
            />
          </Card>
        </Col>
      ))}
    </Row>
  );
}
