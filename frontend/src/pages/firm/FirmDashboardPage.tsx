import { Card, Col, Row, Statistic } from "antd";
import { useEffect, useState } from "react";
import { portalService, type FirmDashboard } from "../../services/portalService";

export function FirmDashboardPage() {
  const [data, setData] = useState<FirmDashboard | null>(null);
  useEffect(() => { void portalService.firmDashboard().then((response) => setData(response.data)); }, []);

  const dashboard = data ?? {
    totalStaff: 0,
    trainedStaff: 0,
    staffInTraining: 0,
    assignedStaff: 0,
    availableTrainedStaff: 0,
    delegated: false,
  } satisfies FirmDashboard;

  const statistics = [
    ["Total staff", dashboard.totalStaff],
    ["Trained staff", dashboard.trainedStaff],
    ["In training", dashboard.staffInTraining],
    ["Assigned staff", dashboard.assignedStaff],
    ["Available trained", dashboard.availableTrainedStaff],
  ] as const;

  return (
    <>
      <Row gutter={16} className="dashboard-cards-row">
        {statistics.map(([title, value]) => (
          <Col span={8} key={title}>
            <Card>
              <Statistic title={title} value={value} />
            </Card>
          </Col>
        ))}
      </Row>
      <div
        className={`firm-delegation-status ${dashboard.delegated ? "is-active" : "is-inactive"}`}
        role="status"
        aria-live="polite"
      >
        <span className="firm-delegation-status__dot" aria-hidden="true" />
        <span className="firm-delegation-status__copy">
          <strong>{dashboard.delegated ? "Firm delegated" : "No active delegation"}</strong>
          <small>{dashboard.delegated ? "Delegation is currently available" : "Waiting for a delegator assignment"}</small>
        </span>
      </div>
    </>
  );
}