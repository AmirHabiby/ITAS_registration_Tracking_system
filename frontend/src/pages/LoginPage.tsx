import { Button, Form, Input, message } from "antd";
import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import logo from "../assets/logo_no_bg.png";

export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  if (user) {
    return <Navigate to="/" replace />;
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: 24,
        background: "#ffffff",
      }}
    >
      <style>{`
        .login-container {
          width: 100%;
          max-width: 420px;
        }

        .login-logo {
          text-align: center;
          margin-bottom: 48px;
        }

        .login-logo img {
          height: 130px;
          width: auto;
        }

        .login-form-wrapper {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .login-form-item {
          margin-bottom: 0;
        }

        .login-input-field {
          position: relative;
          background-color: #fff !important;
          border: 1px solid #ccc !important;
          border-radius: 8px !important;
          padding: 0 16px !important;
          height: 48px !important;
          color: #1a1a1a !important;
          font-size: 14px !important;
          transition: all 0.3s ease !important;
        }

        .login-input-field:hover,
        .login-input-field:focus,
        .login-input-field:focus-within {
          border-color: #0e79bf !important;
        }

        .login-input-field input {
          background-color: transparent !important;
          color: #1a1a1a !important;
          font-size: 14px !important;
          border: none !important;
          padding: 0 !important;
          height: 100% !important;
        }

        .login-input-field::placeholder,
        .login-input-field input::placeholder {
          color: #999 !important;
        }

        .ant-input-password-icon {
          color: #999 !important;
        }

        .login-submit-btn {
          background-color: #0e79bf !important;
          border: none !important;
          color: #fff !important;
          font-size: 16px !important;
          font-weight: 600 !important;
          height: 48px !important;
          border-radius: 8px !important;
          transition: all 0.3s ease !important;
          margin-top: 8px;
        }

        .login-submit-btn:hover {
          background-color: #0a5a8f !important;
          color: #fff !important;
        }

        .login-submit-btn:active {
          background-color: #084570 !important;
        }

        .login-footer-text {
          text-align: center;
          color: #999;
          font-size: 12px;
          margin-top: 24px;
          line-height: 1.6;
        }

        .login-footer-text a {
          color: #0e79bf;
          text-decoration: underline;
          cursor: pointer;
        }

        .login-footer-text a:hover {
          color: #0a5a8f;
        }

        .login-links {
          display: flex;
          justify-content: center;
          align-items: center;
          margin-top: 16px;
          font-size: 12px;
        }

        .login-links a {
          color: #0e79bf;
          text-decoration: underline;
          cursor: pointer;
        }

        .login-links a:hover {
          color: #0a5a8f;
        }

        .login-guest-link {
          text-align: center;
          margin-top: 24px;
          font-size: 12px;
        }

        .login-guest-link a {
          color: #999;
          text-decoration: none;
        }

        .login-guest-link a:hover {
          color: #bbb;
        }
      `}</style>

      <div className="login-container">
        <div className="login-logo">
          <img src={logo} alt="Ministry of Revenues" />
        </div>

        <Form
          className="login-form-wrapper"
          layout="vertical"
          requiredMark={false}
          onFinish={async (values) => {
            setLoading(true);
            try {
              await login(values.username, values.password);
              message.success("Logged in");
              void navigate("/");
            } catch {
              message.error("Login failed");
            } finally {
              setLoading(false);
            }
          }}
        >
          <Form.Item
            name="username"
            className="login-form-item"
            rules={[{ required: true, message: "Please enter your username" }]}
          >
            <Input
              placeholder="Username / Email"
              className="login-input-field"
            />
          </Form.Item>

          <Form.Item
            name="password"
            className="login-form-item"
            rules={[{ required: true, message: "Please enter your password" }]}
          >
            <Input.Password
              placeholder="Password"
              className="login-input-field"
            />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            block
            className="login-submit-btn"
          >
            {loading ? "Signing in..." : "Sign in"}
          </Button>
        </Form>

        <div className="login-links">
          <Link to="/" style={{ color: "#0e79bf", textDecoration: "underline", cursor: "pointer" }}>
            Forgot password?
          </Link>
        </div>

        <div className="login-footer-text">
          Or view <Link to="/public/trainings">public</Link> or <Link to="/staff/trainings">staff</Link> trainings
        </div>
      </div>
    </div>
  );
}
