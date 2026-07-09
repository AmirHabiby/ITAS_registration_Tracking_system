import { Layout, Menu, Typography, Button, Space } from "antd";
import { useMemo } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const { Header, Sider, Content } = Layout;

const menus: Record<string, { key: string; label: string; path: string }[]> = {
  SYSTEM_ADMIN: [
    { key: "/admin/dashboard", label: "Dashboard", path: "/admin/dashboard" },
    { key: "/admin/users", label: "Users", path: "/admin/users" },
    {
      key: "/admin/representatives",
      label: "Representatives",
      path: "/admin/representatives",
    },
    {
      key: "/admin/delegators",
      label: "Delegators",
      path: "/admin/delegators",
    },
    {
      key: "/admin/training-institutes",
      label: "Training Institutes",
      path: "/admin/training-institutes",
    },
  ],
  REPRESENTATIVE: [
    {
      key: "/representative/dashboard",
      label: "Dashboard",
      path: "/representative/dashboard",
    },
    {
      key: "/representative/trainings",
      label: "Trainings",
      path: "/representative/trainings",
    },
    {
      key: "/representative/my-requests",
      label: "My Requests",
      path: "/representative/my-requests",
    },
    {
      key: "/representative/my-results",
      label: "My Results",
      path: "/representative/my-results",
    },
  ],
  DELEGATOR: [
    {
      key: "/delegator/dashboard",
      label: "Dashboard",
      path: "/delegator/dashboard",
    },
    {
      key: "/delegator/training-requests",
      label: "Training Requests",
      path: "/delegator/training-requests",
    },
    {
      key: "/delegator/trained-representatives",
      label: "Trained Representatives",
      path: "/delegator/trained-representatives",
    },
    { key: "/delegator/agents", label: "Agents", path: "/delegator/agents" },
  ],
  TRAINING_INSTITUTE: [
    {
      key: "/institute/dashboard",
      label: "Dashboard",
      path: "/institute/dashboard",
    },
    {
      key: "/institute/trainings",
      label: "Trainings",
      path: "/institute/trainings",
    },
    {
      key: "/institute/enrollments",
      label: "Enrollments",
      path: "/institute/enrollments",
    },
    {
      key: "/institute/assessments",
      label: "Assessments",
      path: "/institute/assessments",
    },
  ],
};

export function RoleLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const items = useMemo(() => menus[user?.role ?? ""] ?? [], [user?.role]);

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider width={260} theme="dark" style={{ paddingTop: 16 }}>
        <div
          style={{ color: "white", padding: "0 20px 20px", fontWeight: 700 }}
        >
          RTTS
          <div style={{ fontSize: 12, opacity: 0.7, fontWeight: 400 }}>
            {user?.displayName}
          </div>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={items.map((item) => ({
            key: item.key,
            label: <Link to={item.path}>{item.label}</Link>,
          }))}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: "#fff",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Typography.Text strong>{user?.role}</Typography.Text>
          <Space>
            <Typography.Text>{user?.username}</Typography.Text>
            <Button
              onClick={() => {
                logout();
                void navigate("/login");
              }}
            >
              Logout
            </Button>
          </Space>
        </Header>
        <Content style={{ padding: 24 }}>{children}</Content>
      </Layout>
    </Layout>
  );
}
