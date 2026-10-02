import { Avatar, Badge, Button, Dropdown, Form, Image, Input, Layout, Menu, Modal, Popover, Space, Typography, Upload, message } from "antd";
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
import { portalService } from "../services/portalService";
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
  const { user, logout, refreshUser } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const items = useMemo(() => menus[user?.role ?? ""] ?? [], [user?.role]);
  const canUpdateProfile = ["SYSTEM_ADMIN", "REPRESENTATIVE", "DELEGATOR", "TRAINING_INSTITUTE", "FIRM_ADMIN"]
    .includes(user?.role ?? "");
  const [collapsed, setCollapsed] = useState(false);
  const [notificationSummary, setNotificationSummary] = useState<NotificationSummary>({
    count: 0,
    message: "No new notifications.",
    path: "/",
  });
  const [notificationError, setNotificationError] = useState<string | null>(null);
  const [notificationLoading, setNotificationLoading] = useState(true);
  const [profileModalOpen, setProfileModalOpen] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileImage, setProfileImage] = useState<File>();
  const [profileImagePreview, setProfileImagePreview] = useState<string | null>(null);
  const [profileForm] = Form.useForm<{ fullName: string }>();

  useEffect(() => {
    if (!profileImagePreview?.startsWith("blob:")) return;
    return () => URL.revokeObjectURL(profileImagePreview);
  }, [profileImagePreview]);

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

  async function openProfileEditor() {
    if (profileLoading) return;
    setProfileImage(undefined);
    setProfileImagePreview(null);
    setProfileLoading(true);
    try {
      const response = await portalService.getOwnProfile();
      profileForm.setFieldsValue({ fullName: response.data.fullName });
      setProfileModalOpen(true);
    } catch {
      message.error("Unable to load your profile. Please try again.");
    } finally {
      setProfileLoading(false);
    }
  }

  async function saveProfile(values: { fullName: string }) {
    setProfileSaving(true);
    try {
      await portalService.updateOwnProfile(values.fullName, profileImage);
      setProfileModalOpen(false);
      setProfileImage(undefined);
      setProfileImagePreview(null);
      message.success("Profile updated.");
      try {
        await refreshUser();
      } catch {
        message.error("Profile saved, but the navbar could not refresh. Reload the page to see the latest details.");
      }
    } catch {
      message.error("Unable to update your profile. Please try again.");
    } finally {
      setProfileSaving(false);
    }
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
              {canUpdateProfile ? (
                <Dropdown
                  trigger={["click"]}
                  menu={{
                    items: [{ key: "update-profile", label: "Update profile", disabled: profileLoading }],
                    onClick: () => void openProfileEditor(),
                  }}
                >
                  <Button
                    type="text"
                    className="role-layout-avatar-button"
                    aria-label="Open profile menu"
                    title="Open profile menu"
                  >
                    <Avatar size={40} src={user?.profileImageUrl ?? undefined} icon={<UserOutlined />} />
                  </Button>
                </Dropdown>
              ) : (
                <Popover
                  title={user?.displayName}
                  content={<Typography.Text type="secondary">{user?.username}</Typography.Text>}
                  trigger="click"
                >
                  <Button
                    type="text"
                    className="role-layout-avatar-button"
                    aria-label="Open profile menu"
                    title="Open profile menu"
                  >
                    <Avatar icon={<UserOutlined />} />
                  </Button>
                </Popover>
              )}
            </Space>
          </Header>
          {canUpdateProfile && (
            <Modal
              title="Update profile"
              open={profileModalOpen}
              onCancel={() => {
                setProfileModalOpen(false);
                setProfileImage(undefined);
                setProfileImagePreview(null);
              }}
              footer={null}
              destroyOnClose
            >
              <Form
                form={profileForm}
                layout="vertical"
                disabled={profileLoading}
                onFinish={(values) => void saveProfile(values)}
              >
                <Form.Item
                  name="fullName"
                  label="Name"
                  rules={[
                    { required: true, whitespace: true, message: "Enter your name." },
                    { max: 160, message: "Name must be 160 characters or fewer." },
                  ]}
                >
                  <Input />
                </Form.Item>
                <Form.Item label="Profile picture">
                  <Space direction="vertical" size="middle">
                    <Image
                      src={profileImagePreview ?? user?.profileImageUrl ?? undefined}
                      alt="Profile picture preview"
                      width={100}
                      height={100}
                      preview={false}
                      style={{ borderRadius: "50%", objectFit: "cover" }}
                    />
                  <Upload
                    accept="image/jpeg,image/png,image/webp"
                    beforeUpload={(file) => {
                      if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
                        message.error("Choose a JPEG, PNG, or WebP image.");
                        return Upload.LIST_IGNORE;
                      }
                      if (file.size > 5 * 1024 * 1024) {
                        message.error("Profile images must be 5 MB or smaller.");
                        return Upload.LIST_IGNORE;
                      }
                      setProfileImage(file);
                      setProfileImagePreview(URL.createObjectURL(file));
                      return false;
                    }}
                    onRemove={() => {
                      setProfileImage(undefined);
                      setProfileImagePreview(null);
                    }}
                    showUploadList={false}
                    maxCount={1}
                  >
                    <Button>{profileImage ? "Choose another image" : "Select image"}</Button>
                  </Upload>
                  </Space>
                </Form.Item>
                <Space>
                  <Button onClick={() => setProfileModalOpen(false)}>Cancel</Button>
                  <Button type="primary" htmlType="submit" loading={profileSaving}>
                    Save profile
                  </Button>
                </Space>
              </Form>
            </Modal>
          )}
          <Content className="role-layout-content">{children}</Content>
        </Layout>
      </Layout>
  );
}
