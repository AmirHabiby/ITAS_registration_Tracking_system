import { Alert, Button, Card, Col, Row, Space, Statistic, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { apiClient } from "../../services/apiClient";
import { portalService, type User } from "../../services/portalService";

type AdminDashboardMetrics = {
  totalTrainedRepresentatives: number;
  totalAgents: number;
  pendingRequests: number;
};

const userPageMetricKeys = [
  "totalTrainedRepresentatives",
  "totalAgents",
  "pendingRequests",
] as const satisfies readonly (keyof AdminDashboardMetrics)[];

export function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [metricsError, setMetricsError] = useState<string | null>(null);

  async function loadUsers() {
    try {
      setError(null);
      setUsers((await portalService.listUsers()).data);
    } catch {
      setError("Unable to load users.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadUsers(); }, []);

  useEffect(() => {
    let active = true;
    apiClient.get<AdminDashboardMetrics>("/api/dashboard/admin")
      .then((response) => {
        if (active) setMetrics(response.data);
      })
      .catch(() => {
        if (active) setMetricsError("Unable to load user dashboard metrics.");
      });
    return () => {
      active = false;
    };
  }, []);

  async function setEnabled(user: User, enabled: boolean) {
    setUpdatingId(user.id);
    try {
      await portalService.setUserEnabled(user.id, enabled);
      message.success(`User ${enabled ? "activated" : "deactivated"}.`);
      await loadUsers();
    } catch {
      message.error("Unable to update user status.");
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {metricsError && <Alert message={metricsError} type="error" showIcon />}
      {metrics && (
        <Row gutter={16} className="dashboard-cards-row admin-dashboard-cards admin-users-metrics">
          {userPageMetricKeys.map((key, index) => (
            <Col
              span={8}
              key={key}
              className={
                index === 0
                  ? "admin-dashboard-card-column-1"
                  : index === 1
                    ? "admin-dashboard-card-column-2"
                    : "admin-users-metric-column-3"
              }
            >
              <Card>
                <Statistic title={key} value={metrics[key]} />
              </Card>
            </Col>
          ))}
        </Row>
      )}
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={users} rowKey="id" columns={[
        { title: "Username", dataIndex: "username" },
        { title: "Display name", dataIndex: "displayName" },
        { title: "Role", render: (_, user) => <Tag>{user.role}</Tag> },
        { title: "Status", render: (_, user) => <Tag color={user.enabled ? "green" : "red"}>{user.enabled ? "Enabled" : "Disabled"}</Tag> },
        { title: "Action", render: (_, user) => <Button loading={updatingId === user.id} disabled={updatingId !== null} onClick={() => void setEnabled(user, !user.enabled)}>{updatingId === user.id ? (user.enabled ? "Deactivating..." : "Activating...") : user.enabled ? "Deactivate account" : "Activate account"}</Button> },
      ]} />
    </Space>
  );
}
