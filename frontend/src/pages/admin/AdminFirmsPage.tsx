import { Alert, Button, Card, Form, Input, Space, Switch, Table, Tag, Upload, message } from "antd";
import type { UploadFile } from "antd/es/upload/interface";
import { useEffect, useState } from "react";
import { portalService, type Firm } from "../../services/portalService";

export function AdminFirmsPage() {
  const [firms, setFirms] = useState<Firm[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [adminImage, setAdminImage] = useState<File>();
  const [adminImageList, setAdminImageList] = useState<UploadFile[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function loadFirms() {
    try {
      setError(null);
      setFirms((await portalService.listAdminFirms()).data);
    } catch {
      setError("Unable to load firms.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadFirms(); }, []);

  async function submit(values: Record<string, unknown>) {
    setSubmitting(true);
    try {
      await portalService.createFirm({ ...values, enabled: values.enabled ?? true }, adminImage);
      message.success("Firm and Firm Admin created.");
      await loadFirms();
    } catch {
      message.error("Unable to create firm.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Card title="Create firm and Firm Admin">
        <Form layout="vertical" onFinish={(values) => void submit(values)} initialValues={{ enabled: true }}>
          <Form.Item name="name" label="Firm name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="Firm email" rules={[{ required: true, type: "email" }]}><Input /></Form.Item>
          <Form.Item name="description" label="Description"><Input.TextArea /></Form.Item>
          <Form.Item name="adminUsername" label="Admin username" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="adminPassword" label="Admin password" rules={[{ required: true, min: 8 }]}><Input.Password /></Form.Item>
          <Form.Item name="adminDisplayName" label="Admin display name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="adminFullName" label="Admin full name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="adminEmail" label="Admin email" rules={[{ required: true, type: "email" }]}><Input /></Form.Item>
          <Form.Item label="Firm Admin profile image">
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
                setAdminImage(file);
                setAdminImageList([{ uid: file.uid, name: file.name, status: "done" }]);
                return false;
              }}
              onRemove={() => {
                setAdminImage(undefined);
                setAdminImageList([]);
              }}
              fileList={adminImageList}
              maxCount={1}
            >
              <Button>Select image</Button>
            </Upload>
          </Form.Item>
          <Form.Item name="enabled" label="Enabled" valuePropName="checked"><Switch /></Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>{submitting ? "Creating firm..." : "Create firm and admin"}</Button>
        </Form>
      </Card>
      <Card title="Firms">
        {error && <Alert message={error} type="error" showIcon />}
        <Table loading={loading} dataSource={firms} rowKey="id" columns={[
          { title: "Name", dataIndex: "name" },
          { title: "Email", dataIndex: "email" },
          { title: "Description", dataIndex: "description" },
          { title: "Status", render: (_: unknown, firm: Firm) => <Tag color={firm.active ? "green" : "red"}>{firm.active ? "Active" : "Inactive"}</Tag> },
        ]} />
      </Card>
    </Space>
  );
}