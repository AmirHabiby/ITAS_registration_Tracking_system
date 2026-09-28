import { Avatar, Badge, Button, Layout, Menu, Popover, Space, Typography } from "antd";
import {
  BankOutlined,
  BellOutlined,
  DashboardOutlined,
  FileDoneOutlined,
  FileSearchOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ShopOutlined,
  SolutionOutlined,
  TeamOutlined,
  TrophyOutlined,
  UserOutlined,
  UserSwitchOutlined,
} from "@ant-design/icons";
import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { apiClient } from "../services/apiClient";
import { assessmentService } from "../services/assessmentService";
import logo from "../assets/logo_no_bg.png";

const { Header, Sider, Content } = Layout;

type NotificationSummary = {
  count: number;
  message: string;
  path: string;
};

const menus: Record<
  string,
  { key: string; label: string; path: string; icon: React.ReactNode }[]
> = {
  SYSTEM_ADMIN: [
    { key: "/admin/dashboard", label: "Dashboard", path: "/admin/dashboard", icon: <DashboardOutlined /> },
    { key: "/admin/create-training", label: "Create Training", path: "/admin/create-training", icon: <FileDoneOutlined /> },
    { key: "/admin/users", label: "Users", path: "/admin/users", icon: <UserOutlined /> },
    {
      key: "/admin/representatives",
      label: "Representatives",
      path: "/admin/representatives",
      icon: <TeamOutlined />,
    },
    {
      key: "/admin/delegators",
      label: "Delegators",
      path: "/admin/delegators",
      icon: <SolutionOutlined />,
    },
    {
      key: "/admin/training-institutes",
      label: "Training Institutes",
      path: "/admin/training-institutes",
      icon: <BankOutlined />,
    },
    { key: "/admin/firms", label: "Firms", path: "/admin/firms", icon: <ShopOutlined /> },
  ],
  REPRESENTATIVE: [
    {
      key: "/representative/dashboard",
      label: "Dashboard",
      path: "/representative/dashboard",
      icon: <DashboardOutlined />,
    },
    {
      key: "/representative/trainings",
      label: "Trainings",
      path: "/representative/trainings",
      icon: <BankOutlined />,
    },
    {
      key: "/representative/my-requests",
      label: "My Requests",
      path: "/representative/my-requests",
      icon: <FileTextOutlined />,
    },
    {
      key: "/representative/my-results",
      label: "My Results",
      path: "/representative/my-results",
      icon: <TrophyOutlined />,
    },
    {
      key: "/representative/assessments",
      label: "Online Assessments",
      path: "/representative/assessments",
      icon: <FileSearchOutlined />,
    },
  ],
  DELEGATOR: [
    {
      key: "/delegator/dashboard",
      label: "Dashboard",
      path: "/delegator/dashboard",
      icon: <DashboardOutlined />,
    },
    {
      key: "/delegator/training-requests",
      label: "Training Requests",
      path: "/delegator/training-requests",
      icon: <FileTextOutlined />,
    },
    {
      key: "/delegator/trained-representatives",
      label: "Trained Representatives",
      path: "/delegator/trained-representatives",
      icon: <TeamOutlined />,
    },
    {
      key: "/delegator/agents",
      label: "Agents",
      path: "/delegator/agents",
      icon: <UserSwitchOutlined />,
    },
  ],
  TRAINING_INSTITUTE: [
    {
      key: "/institute/dashboard",
      label: "Dashboard",
      path: "/institute/dashboard",
      icon: <DashboardOutlined />,
    },
    {
      key: "/institute/trainings",
      label: "Trainings",
      path: "/institute/trainings",
      icon: <BankOutlined />,
    },
    {
      key: "/institute/create-trainings",
      label: "Create Trainings",
      path: "/institute/create-trainings",
      icon: <FileDoneOutlined />,
    },
    {
      key: "/institute/enrollments",
      label: "Enrollments",
      path: "/institute/enrollments",
      icon: <SolutionOutlined />,
    },
    {
      key: "/institute/assessments",
      label: "Assessments",
      path: "/institute/assessments",
      icon: <FileDoneOutlined />,
    },
  ],
  FIRM_ADMIN: [
    { key: "/firm/dashboard", label: "Dashboard", path: "/firm/dashboard", icon: <DashboardOutlined /> },
    { key: "/firm/staff", label: "Staff", path: "/firm/staff", icon: <TeamOutlined /> },
  ],
};

export function RoleLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const items = useMemo(() => menus[user?.role ?? ""] ?? [], [user?.role]);
  const [collapsed, setCollapsed] = useState(false);
  const [notificationSummary, setNotificationSummary] = useState<NotificationSummary>({
    count: 0,
    message: "No new notifications.",
    path: "/",
  });
  const [notificationError, setNotificationError] = useState<string | null>(null);
  const [notificationLoading, setNotificationLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadNotifications() {
      if (!user) return;
      setNotificationLoading(true);
      setNotificationError(null);
      try {
        let summary: NotificationSummary;
        switch (user.role) {
          case "SYSTEM_ADMIN": {
            const response = await apiClient.get<{ pendingRequests: number }>("/api/dashboard/admin");
            summary = {
              count: response.data.pendingRequests,
              message: "training request(s) awaiting review",
              path: "/admin/dashboard",
            };
            break;
          }
          case "DELEGATOR": {
            const response = await apiClient.get<{ pendingTrainingRequests: number }>("/api/dashboard/delegator");
            summary = {
              count: response.data.pendingTrainingRequests,
              message: "training request(s) awaiting your decision",
              path: "/delegator/training-requests",
            };
            break;
          }
          case "REPRESENTATIVE": {
            const response = await apiClient.get<{ myPendingRequests: number }>("/api/dashboard/representative");
            summary = {
              count: response.data.myPendingRequests,
              message: "request(s) awaiting a response",
              path: "/representative/my-requests",
            };
            break;
          }
          case "TRAINING_INSTITUTE": {
            const [queue, attempts] = await Promise.all([
              assessmentService.listGradingQueue(),
              assessmentService.listCompletedAttempts(),
            ]);
            const needsGrading = queue.data.filter(
              (item) => item.writtenQuestionCount > item.gradedWrittenQuestionCount,
            ).length;
            const readyToRelease = attempts.data.filter((attempt) => attempt.finalized && !attempt.releasedAt).length;
            summary = {
              count: needsGrading + readyToRelease,
              message: "assessment(s) needing grading or result release",
              path: "/institute/assessments",
            };
            break;
          }
          default:
            summary = { count: 0, message: "No new notifications.", path: "/" };
        }
        if (active) setNotificationSummary(summary);
      } catch {
        if (active) setNotificationError("Unable to load notifications. Please try again.");
      } finally {
        if (active) setNotificationLoading(false);
      }
    }

    void loadNotifications();
    const refreshTimer = window.setInterval(() => void loadNotifications(), 60_000);
    return () => {
      active = false;
      window.clearInterval(refreshTimer);
    };
  }, [user]);

  function signOut() {
    logout();
    void navigate("/login");
  }

  const notificationContent = notificationError
    ? <Typography.Text type="danger">{notificationError}</Typography.Text>
    : notificationLoading
      ? <Typography.Text type="secondary">Loading notifications…</Typography.Text>
      : notificationSummary.count === 0
        ? <Typography.Text type="secondary">{notificationSummary.message}</Typography.Text>
        : (
          <Space direction="vertical" size="small">
            <Typography.Text>
              {notificationSummary.count} {notificationSummary.message}
            </Typography.Text>
            <Button type="link" onClick={() => void navigate(notificationSummary.path)}>
              View items
            </Button>
          </Space>
        );

  return (
      <Layout className="role-layout-root">
        <Sider
          width={260}
          breakpoint="lg"
          collapsible
          collapsedWidth={0}
          collapsed={collapsed}
          onCollapse={setCollapsed}
          trigger={null}
          className="sidebar-layout"
          theme="light"
          style={{ paddingTop: 16, background: "#fff" }}
        >
          <div
            style={{
              color: "#0e79bf",
              padding: "0 20px 20px",
              fontWeight: 700,
              display: "flex",
              alignItems: "center",
              gap: 10,
            }}
          >
            <img src={logo} alt="Ministry of Revenues" style={{ height: 56, width: "auto" }} />
            <div style={{ fontSize: 15, opacity: 0.7, fontWeight: 700, color: "#0e79bf" }}>
              {user?.displayName}
            </div>
          </div>
          <Menu
            theme="light"
            mode="inline"
            selectedKeys={[location.pathname.startsWith("/representative/assessment-attempts/")
              ? "/representative/assessments"
              : location.pathname]}
            items={items.map((item) => ({
              key: item.key,
              icon: item.icon,
              label: <Link to={item.path} style={{ color: "inherit" }}>{item.label}</Link>,
            }))}
          />
          <Button
            className="role-layout-signout sign-out-button"
            icon={<LogoutOutlined />}
            onClick={signOut}
          >
            Sign out
          </Button>
        </Sider>
        {!collapsed && (
          <button
            type="button"
            className="role-layout-overlay"
            aria-label="Close navigation menu"
            onClick={() => setCollapsed(true)}
          />
        )}
        <Layout className={`role-layout-main${collapsed ? " role-layout-main-collapsed" : ""}`}>
          <Header className="role-layout-header">
            <Space size="middle">
              <Button
                className="role-layout-menu-toggle"
                type="text"
                aria-label={collapsed ? "Open navigation menu" : "Collapse navigation menu"}
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed((current) => !current)}
              />
              <Typography.Text strong>{user?.role?.replaceAll("_", " ")}</Typography.Text>
            </Space>
            <Space size="middle">
              <Popover content={notificationContent} title="Notifications" trigger="click">
                <Badge count={notificationSummary.count} overflowCount={99} size="small" color="#0e79bf">
                  <Button
                    type="text"
                    className="role-layout-icon-button"
                    aria-label={`Notifications: ${notificationSummary.count}`}
                    icon={<BellOutlined />}
                  />
                </Badge>
              </Popover>
              <Popover
                title={user?.displayName}
                content={<Typography.Text type="secondary">{user?.username}</Typography.Text>}
                trigger="click"
              >
                <Button type="text" className="role-layout-avatar-button" aria-label="Open profile menu">
                  <Avatar icon={<UserOutlined />} />
                </Button>
              </Popover>
            </Space>
          </Header>
          <Content className="role-layout-content">{children}</Content>
        </Layout>
      </Layout>
  );
}
