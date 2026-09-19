import { Card, Col, Row, Statistic, Tag } from "antd";
import { useEffect, useState } from "react";
import { portalService, type FirmDashboard } from "../../services/portalService";

export function FirmDashboardPage() {
  const [data, setData] = useState<FirmDashboard | null>(null);
  useEffect(() => { void portalService.firmDashboard().then((response) => setData(response.data)); }, []);
  return <Card title="Firm dashboard">
    <Row gutter={[16, 16]}>
      {data && Object.entries({ "Total staff": data.totalStaff, "Trained staff": data.trainedStaff, "In training": data.staffInTraining, "Assigned staff": data.assignedStaff, "Available trained": data.availableTrainedStaff }).map(([title, value]) => <Col xs={24} sm={12} lg={8} key={title}><Statistic title={title} value={value} /></Col>)}
    </Row>
    <Tag color={data?.delegated ? "green" : "default"}>{data?.delegated ? "Firm delegated" : "No active delegation"}</Tag>
  </Card>;
}