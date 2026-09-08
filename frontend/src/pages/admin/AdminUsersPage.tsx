import { Alert, Button, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type User } from "../../services/portalService";

export function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  async function setEnabled(user: User, enabled: boolean) {
    try {
      await portalService.setUserEnabled(user.id, enabled);
      message.success(`User ${enabled ? "activated" : "deactivated"}.`);
      await loadUsers();
    } catch {
      message.error("Unable to update user status.");
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={users} rowKey="id" columns={[
        { title: "Username", dataIndex: "username" },
        { title: "Display name", dataIndex: "displayName" },
        { title: "Role", render: (_, user) => <Tag>{user.role}</Tag> },
        { title: "Status", render: (_, user) => <Tag color={user.enabled ? "green" : "red"}>{user.enabled ? "Enabled" : "Disabled"}</Tag> },
        { title: "Action", render: (_, user) => <Button onClick={() => void setEnabled(user, !user.enabled)}>{user.enabled ? "Deactivate" : "Activate"}</Button> },
      ]} />
    </Space>
  );
}
