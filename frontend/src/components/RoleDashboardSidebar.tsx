import { Calendar, Card, Col } from "antd";
import { SystemAnnouncement } from "./SystemAnnouncement";

export function RoleDashboardSidebar() {
  return (
    <Col span={8} className="role-dashboard-third-column">
      <Card title="Calendar" className="role-dashboard-calendar-card">
        <Calendar className="admin-dashboard-calendar" fullscreen={false} />
      </Card>
      <Card title="Notification" className="role-dashboard-notification-card">
        <SystemAnnouncement />
      </Card>
    </Col>
  );
}
