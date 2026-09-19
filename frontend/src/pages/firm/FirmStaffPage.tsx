import { Button, Card, Form, Input, Space, Switch, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type FirmStaff } from "../../services/portalService";

export function FirmStaffPage() {
  const [staff, setStaff] = useState<FirmStaff[]>([]);
  const [delegated, setDelegated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  async function load() {
    try {
      const [staffResponse, delegationResponse] = await Promise.all([portalService.listFirmStaff(), portalService.getFirmDelegation()]);
      setStaff(staffResponse.data);
      setDelegated(Boolean(delegationResponse.data));
    } catch {
      message.error("Unable to load firm staff.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  async function createStaff(values: Record<string, unknown>) {
    setCreating(true);
    try {
      await portalService.createFirmStaff({ ...values, enabled: values.enabled ?? true });
      message.success("Staff member created.");
      await load();
    } catch {
      message.error("Unable to create staff member.");
    } finally {
      setCreating(false);
    }
  }

  async function assign(id: string) {
    try { await portalService.assignFirmAgent(id, "Assigned by Firm Admin"); message.success("Staff member assigned as agent."); await load(); }
    catch { message.error("Unable to assign staff member."); }
  }
  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Card title="Add staff member">
        <Form layout="vertical" onFinish={(values) => void createStaff(values)} initialValues={{ enabled: true }}>
          <Form.Item name="username" label="Username" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="password" label="Temporary password" rules={[{ required: true, min: 8 }]}><Input.Password /></Form.Item>
          <Form.Item name="displayName" label="Display name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="fullName" label="Full name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: "email" }]}><Input /></Form.Item>
          <Form.Item name="enabled" label="Enabled" valuePropName="checked"><Switch /></Form.Item>
          <Button type="primary" htmlType="submit" loading={creating}>Create staff member</Button>
        </Form>
      </Card>
      <Card title="Firm staff">
        <Table loading={loading} rowKey="id" dataSource={staff} columns={[
          { title: "Name", dataIndex: "fullName" }, { title: "Email", dataIndex: "email" }, { title: "Training status", dataIndex: "status", render: (status: string) => <Tag>{status}</Tag> },
          { title: "Actions", render: (_: unknown, record: FirmStaff) => <Space><Button disabled={!delegated || record.status !== "TRAINED"} onClick={() => void assign(record.id)}>Assign as agent</Button></Space> },
        ]} />
      </Card>
    </Space>
  );
}