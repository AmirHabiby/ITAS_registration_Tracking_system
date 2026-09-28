import { InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, InputNumber, Select, Space, Upload, message } from 'antd';
import type { RcFile, UploadFile } from 'antd/es/upload/interface';
import { useEffect, useState } from 'react';
import { portalService, type Institute, type Training } from '../../services/portalService';

const { Dragger } = Upload;

type TrainingForm = {
  instituteId: string;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  capacity: number;
  accessType: 'PUBLIC' | 'STAFF';
  staffAccessPassword?: string;
  weeks: number;
};

export function AdminCreateTrainingPage() {
  const [institutes, setInstitutes] = useState<Institute[]>([]);
  const [training, setTraining] = useState<Training | null>(null);
  const [weeks, setWeeks] = useState(1);
  const [files, setFiles] = useState<Record<number, UploadFile[]>>({});
  const [submitting, setSubmitting] = useState(false);
  const [uploadingWeek, setUploadingWeek] = useState<number | null>(null);

  useEffect(() => {
    void portalService.listInstitutes()
      .then((response) => setInstitutes(response.data.filter((institute) => institute.active)))
      .catch(() => message.error('Unable to load training institutes.'));
  }, []);

  async function createTraining(values: TrainingForm) {
    setSubmitting(true);
    try {
      const { weeks: selectedWeeks, ...trainingValues } = values;
      const response = await portalService.createAdminTraining(trainingValues);
      setTraining(response.data);
      setWeeks(selectedWeeks);
      message.success('Training created. You can now upload weekly materials.');
    } catch {
      message.error('Unable to create training.');
    } finally {
      setSubmitting(false);
    }
  }

  async function uploadWeekMaterial(weekNumber: number) {
    const selectedFiles = (files[weekNumber] ?? [])
      .map((file) => file.originFileObj)
      .filter((file): file is RcFile => Boolean(file));
    if (!training || selectedFiles.length === 0) {
      message.warning(`Select a file for week ${weekNumber} first.`);
      return;
    }
    setUploadingWeek(weekNumber);
    try {
      await Promise.all(selectedFiles.map((file) =>
        portalService.uploadTrainingMaterial(training.id, weekNumber, file, `Week ${weekNumber} material`)));
      message.success(`${selectedFiles.length} material(s) uploaded for week ${weekNumber}.`);
      setFiles((current) => ({ ...current, [weekNumber]: [] }));
    } catch {
      message.error(`Unable to upload week ${weekNumber} material.`);
    } finally {
      setUploadingWeek(null);
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title="Create Training">
        <Form layout="vertical" onFinish={(values) => void createTraining(values)} disabled={Boolean(training)}>
          <Form.Item name="instituteId" label="Training institute" rules={[{ required: true }]}>
            <Select options={institutes.map((institute) => ({ value: institute.id, label: `${institute.name} (${institute.contactEmail})` }))} />
          </Form.Item>
          <Form.Item name="title" label="Title" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description" rules={[{ required: true }]}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Space style={{ display: 'flex' }} align="start">
            <Form.Item name="startDate" label="Start date" rules={[{ required: true }]}>
              <Input type="date" />
            </Form.Item>
            <Form.Item name="endDate" label="End date" rules={[{ required: true }]}>
              <Input type="date" />
            </Form.Item>
            <Form.Item name="capacity" label="Capacity" rules={[{ required: true }]}>
              <InputNumber min={1} />
            </Form.Item>
          </Space>
          <Form.Item name="accessType" label="Access type" initialValue="PUBLIC" rules={[{ required: true }]}>
            <Select options={[{ value: 'PUBLIC', label: 'Public' }, { value: 'STAFF', label: 'Staff' }]} />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(previous, current) => previous.accessType !== current.accessType}>
            {({ getFieldValue }) => getFieldValue('accessType') === 'STAFF' ? (
              <Form.Item name="staffAccessPassword" label="Staff access password" rules={[{ required: true, min: 8 }]}>
                <Input.Password />
              </Form.Item>
            ) : null}
          </Form.Item>
          <Form.Item name="weeks" label="Number of weeks" initialValue={1} rules={[{ required: true }]}>
            <InputNumber min={1} max={52} onChange={(value) => setWeeks(Number(value ?? 1))} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>
            Create training
          </Button>
        </Form>
      </Card>

      {training && (
        <Card title="Upload course materials by week">
          <Alert message="Only public and staff trainings can be created here. Materials are uploaded to the selected week." type="info" showIcon />
          <Space direction="vertical" size="middle" style={{ width: '100%', marginTop: 16 }}>
            {Array.from({ length: weeks }, (_, index) => index + 1).map((weekNumber) => (
              <Card size="small" title={`Week ${weekNumber}`} key={weekNumber}>
                <Dragger
                  multiple
                  fileList={files[weekNumber] ?? []}
                  beforeUpload={() => false}
                  onChange={({ fileList }) => setFiles((current) => ({ ...current, [weekNumber]: fileList.slice(-1) }))}
                >
                  <p className="ant-upload-drag-icon"><InboxOutlined /></p>
                  <p>Click or drag videos/documents here</p>
                </Dragger>
                <Button
                  type="primary"
                  style={{ marginTop: 12 }}
                  loading={uploadingWeek === weekNumber}
                  onClick={() => void uploadWeekMaterial(weekNumber)}
                >
                  Upload week {weekNumber} material
                </Button>
              </Card>
            ))}
          </Space>
        </Card>
      )}
    </Space>
  );
}
