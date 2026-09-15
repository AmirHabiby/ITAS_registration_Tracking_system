import { Button, Card, Form, Input, message, Typography } from "antd";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();

  if (user) {
    return <Navigate to="/" replace />;
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        padding: 24,
      }}
    >
      <Card style={{ width: "100%", maxWidth: 360, minHeight: 420 }}>
        <Typography.Title level={3} style={{ textAlign: "center" }}>
          Sign in
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ textAlign: "center", fontSize: 12 }}>
          Or go to <Link to="/public/trainings">public</Link> or <Link to="/staff/trainings">staff</Link> trainings
        </Typography.Paragraph>
        <Form
          className="login-form"
          layout="vertical"
          requiredMark={false}
          style={{ marginTop: 8 }}
          onFinish={async (values) => {
            try {
              await login(values.username, values.password);
              message.success("Logged in");
              void navigate("/");
            } catch {
              message.error("Login failed");
            }
          }}
        >
          <Form.Item
            name="username"
            label="Username"
            style={{ marginBottom: 16 }}
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="password"
            label="Password"
            style={{ marginBottom: 16 }}
            rules={[{ required: true }]}
          >
            <Input.Password />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Login
          </Button>
        </Form>
      </Card>
    </div>
  );
}
