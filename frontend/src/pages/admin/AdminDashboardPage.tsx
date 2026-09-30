import { Alert, Button, Calendar, Card, Col, Input, Row, Statistic, message } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";
import { portalService } from "../../services/portalService";

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

const genderCounts = {
  male: 37,
  female: 63,
};

const monthlyActiveUsers = [
  { week: "Week 1", users: 118 },
  { week: "Week 2", users: 156 },
  { week: "Week 3", users: 143 },
  { week: "Week 4", users: 204 },
  { week: "Week 5", users: 231 },
];

export function AdminDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [announcementText, setAnnouncementText] = useState("");
  const [announcementLoading, setAnnouncementLoading] = useState(true);
  const [announcementSaving, setAnnouncementSaving] = useState(false);
  const [announcementError, setAnnouncementError] = useState<string | null>(null);

  useEffect(() => {
    void apiClient
      .get<Dashboard>("/api/dashboard/admin")
      .then((response) => setData(response.data));
  }, []);

  useEffect(() => {
    let active = true;
    portalService.getSystemAnnouncement()
      .then((response) => {
        if (active) setAnnouncementText(response.data.text);
      })
      .catch(() => {
        if (active) setAnnouncementError("Unable to load the system notification.");
      })
      .finally(() => {
        if (active) setAnnouncementLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  async function saveAnnouncement() {
    setAnnouncementSaving(true);
    setAnnouncementError(null);
    try {
      const response = await portalService.updateSystemAnnouncement(announcementText);
      setAnnouncementText(response.data.text);
      message.success("System notification saved.");
    } catch {
      setAnnouncementError("Unable to save the system notification. Please try again.");
    } finally {
      setAnnouncementSaving(false);
    }
  }

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

  const totalGenderCount = genderCounts.male + genderCounts.female;
  const chartWidth = 500;
  const chartHeight = 200;
  const chartPadding = { top: 16, right: 18, bottom: 36, left: 42 };
  const chartPlotWidth = chartWidth - chartPadding.left - chartPadding.right;
  const chartPlotHeight = chartHeight - chartPadding.top - chartPadding.bottom;
  const chartMaximum = Math.ceil(Math.max(...monthlyActiveUsers.map(({ users }) => users)) / 50) * 50;
  const chartPoints = monthlyActiveUsers.map(({ users }, index) => ({
    x: chartPadding.left + (chartPlotWidth * index) / (monthlyActiveUsers.length - 1),
    y: chartPadding.top + chartPlotHeight * (1 - users / chartMaximum),
  }));
  const chartPolyline = chartPoints.map(({ x, y }) => `${x},${y}`).join(" ");

  return (
    <Row gutter={16} className="dashboard-cards-row admin-dashboard-cards">
      {Object.entries(dashboard).slice(0, 4).map(([key, value], index) => (
        <Col span={8} key={key} className={index % 2 === 0 ? "admin-dashboard-card-column-1" : "admin-dashboard-card-column-2"}>
          <Card>
            <Statistic title={key} value={value} />
          </Card>
        </Col>
      ))}
      <Col span={8} className="admin-dashboard-gender-column">
        <Card title="Registered by gender" className="admin-dashboard-chart-card admin-dashboard-gender-card">
          <div className="admin-dashboard-gender-chart">
            <svg
              className="admin-dashboard-gender-donut"
              viewBox="0 0 180 180"
              role="img"
              aria-label={`Registered representatives: ${genderCounts.male} male and ${genderCounts.female} female`}
            >
              <circle className="admin-dashboard-donut-track" cx="90" cy="90" r="64" />
              <circle
                className="admin-dashboard-donut-male"
                cx="90"
                cy="90"
                r="64"
                strokeDasharray={`${(genderCounts.male / totalGenderCount) * 402.12} 402.12`}
                transform="rotate(-90 90 90)"
              />
              <circle
                className="admin-dashboard-donut-female"
                cx="90"
                cy="90"
                r="64"
                strokeDasharray={`${(genderCounts.female / totalGenderCount) * 402.12} 402.12`}
                strokeDashoffset={`${-((genderCounts.male / totalGenderCount) * 402.12)}`}
                transform="rotate(-90 90 90)"
              />
              <text className="admin-dashboard-donut-total" x="90" y="88" textAnchor="middle">
                {totalGenderCount.toLocaleString()}
              </text>
              <text className="admin-dashboard-donut-caption" x="90" y="108" textAnchor="middle">
                registered
              </text>
            </svg>
            <div className="admin-dashboard-gender-legend" aria-label="Gender counts">
              <div className="admin-dashboard-legend-item">
                <span className="admin-dashboard-legend-dot admin-dashboard-legend-dot-male" />
                <span>Male</span>
                <strong>{genderCounts.male.toLocaleString()}</strong>
              </div>
              <div className="admin-dashboard-legend-item">
                <span className="admin-dashboard-legend-dot admin-dashboard-legend-dot-female" />
                <span>Female</span>
                <strong>{genderCounts.female.toLocaleString()}</strong>
              </div>
            </div>
          </div>
          <p className="admin-dashboard-chart-note">Sample registration counts</p>
        </Card>
      </Col>
      <Col span={8} className="admin-dashboard-active-column">
        <Card title="Active users this month" className="admin-dashboard-chart-card admin-dashboard-active-card">
          <svg
            className="admin-dashboard-active-chart"
            viewBox={`0 0 ${chartWidth} ${chartHeight}`}
            role="img"
            aria-label="Weekly active users this month: 118, 156, 143, 204, and 231"
          >
            {[0, 1, 2, 3].map((step) => {
              const y = chartPadding.top + (chartPlotHeight * step) / 3;
              const value = chartMaximum - (chartMaximum * step) / 3;
              return (
                <g key={step}>
                  <line
                    className="admin-dashboard-chart-gridline"
                    x1={chartPadding.left}
                    x2={chartWidth - chartPadding.right}
                    y1={y}
                    y2={y}
                  />
                  <text className="admin-dashboard-chart-axis-label" x={chartPadding.left - 8} y={y + 4} textAnchor="end">
                    {Math.round(value)}
                  </text>
                </g>
              );
            })}
            <polyline className="admin-dashboard-active-line" points={chartPolyline} />
            {chartPoints.map(({ x, y }, index) => (
              <g key={monthlyActiveUsers[index].week}>
                <circle className="admin-dashboard-active-point" cx={x} cy={y} r="5" />
                <text className="admin-dashboard-chart-axis-label" x={x} y={chartHeight - 10} textAnchor="middle">
                  {`W${index + 1}`}
                </text>
              </g>
            ))}
          </svg>
          <p className="admin-dashboard-chart-note">Weekly active users - sample data</p>
        </Card>
      </Col>
      <Col span={8} className="admin-dashboard-third-column">
        <Card title="Calendar" className="admin-dashboard-calendar-card">
          <Calendar className="admin-dashboard-calendar" fullscreen={false} />
        </Card>
        <Card title="Notification" className="admin-dashboard-notification-card">
          {announcementError && <Alert type="error" showIcon message={announcementError} />}
          <Input.TextArea
            aria-label="System-wide notification"
            value={announcementText}
            onChange={(event) => setAnnouncementText(event.target.value)}
            placeholder="Write a notification to display on every role's main dashboard."
            maxLength={2000}
            showCount
            rows={4}
            disabled={announcementLoading || announcementSaving}
          />
          <Button
            className="admin-dashboard-notification-save"
            type="primary"
            loading={announcementSaving}
            disabled={announcementLoading}
            onClick={() => void saveAnnouncement()}
          >
            Save notification
          </Button>
        </Card>
      </Col>
    </Row>
  );
}
