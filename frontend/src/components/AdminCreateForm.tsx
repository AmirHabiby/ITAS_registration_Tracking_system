import { Button, Form, Input, Switch, Upload, message } from "antd";
import type { UploadFile } from "antd/es/upload/interface";
import { useState } from "react";

type AdminCreateFormProps = {
  kind: "representative" | "delegator" | "institute";
  onSubmit: (body: Record<string, unknown>, image?: File) => Promise<void>;
};

export function AdminCreateForm({ kind, onSubmit }: AdminCreateFormProps) {
  const [loading, setLoading] = useState(false);
  const [image, setImage] = useState<File>();
  const [imageList, setImageList] = useState<UploadFile[]>([]);
  const isInstitute = kind === "institute";

  async function submit(values: Record<string, unknown>) {
    setLoading(true);
    try {
      await onSubmit({ ...values, enabled: values.enabled ?? true }, image);
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
      <Form.Item label="Profile image">
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
            setImage(file);
            setImageList([{ uid: file.uid, name: file.name, status: "done" }]);
            return false;
          }}
          onRemove={() => {
            setImage(undefined);
            setImageList([]);
          }}
          fileList={imageList}
          maxCount={1}
        >
          <Button>Select image</Button>
        </Upload>
      </Form.Item>
      <Form.Item name="enabled" label="Enabled" valuePropName="checked"><Switch /></Form.Item>
      <Button type="primary" htmlType="submit" loading={loading}>{loading ? "Creating account..." : `Create ${isInstitute ? "training institute" : kind}`}</Button>
    </Form>
  );
}