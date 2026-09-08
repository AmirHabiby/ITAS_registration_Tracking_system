import { Button, Form, Input, Switch, message } from "antd";
import { useState } from "react";

type AdminCreateFormProps = {
  kind: "representative" | "delegator" | "institute";
  onSubmit: (body: Record<string, unknown>) => Promise<void>;
};

export function AdminCreateForm({ kind, onSubmit }: AdminCreateFormProps) {
  const [loading, setLoading] = useState(false);
  const isInstitute = kind === "institute";

  async function submit(values: Record<string, unknown>) {
    setLoading(true);
    try {
      await onSubmit({ ...values, enabled: values.enabled ?? true });
      message.success(`${isInstitute ? "Training institute" : kind} created.`);
    } catch {
      message.error(`Unable to create the ${isInstitute ? "training institute" : kind}.`);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Form layout="vertical" onFinish={(values) => void submit(values)} initialValues={{ enabled: true }}>
      <Form.Item name="username" label="Username" rules={[{ required: true }]}><Input /></Form.Item>
      <Form.Item name="password" label="Temporary password" rules={[{ required: true, min: 8 }]}><Input.Password /></Form.Item>
      <Form.Item name="displayName" label="Display name" rules={[{ required: true }]}><Input /></Form.Item>
      {isInstitute ? <>
        <Form.Item name="name" label="Institute name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="contactEmail" label="Contact email" rules={[{ required: true, type: "email" }]}><Input /></Form.Item>
      </> : <>
        <Form.Item name="fullName" label="Full name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="email" label="Email" rules={[{ required: true, type: "email" }]}><Input /></Form.Item>
      </>}
      <Form.Item name="enabled" label="Enabled" valuePropName="checked"><Switch /></Form.Item>
      <Button type="primary" htmlType="submit" loading={loading}>Create</Button>
    </Form>
  );
}