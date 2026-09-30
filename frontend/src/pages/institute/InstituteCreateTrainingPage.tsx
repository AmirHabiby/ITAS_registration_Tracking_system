import { InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Col, Form, Input, InputNumber, Row, Select, Space, Upload, message } from 'antd';
import type { RcFile, UploadFile } from 'antd/es/upload/interface';
import { useState } from 'react';
import { portalService, type Training } from '../../services/portalService';

const { Dragger } = Upload;

type TrainingForm = {
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  capacity: number;
  accessType: 'PRIVATE' | 'PUBLIC' | 'STAFF';
  staffAccessPassword?: string;
  weeks: number;
};

export function InstituteCreateTrainingPage() {
  const [createdTraining, setCreatedTraining] = useState<Training | null>(null);
  const [weeks, setWeeks] = useState(1);
  const [files, setFiles] = useState<Record<number, UploadFile[]>>({});
  const [submitting, setSubmitting] = useState(false);
  const [uploadingWeek, setUploadingWeek] = useState<number | null>(null);

  async function createTraining(values: TrainingForm) {
    setSubmitting(true);
    try {
      const { weeks: selectedWeeks, ...trainingValues } = values;
      const response = await portalService.createInstituteTraining(trainingValues);
      setCreatedTraining(response.data);
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
    if (!createdTraining || selectedFiles.length === 0) {
      message.warning(`Select a file for week ${weekNumber} first.`);
      return;
    }

    setUploadingWeek(weekNumber);
    try {
      await Promise.all(selectedFiles.map((file) =>
        portalService.uploadTrainingMaterial(createdTraining.id, weekNumber, file)));
      message.success(`${selectedFiles.length} material(s) uploaded for week ${weekNumber}.`);
      setFiles((current) => ({ ...current, [weekNumber]: [] }));
    } catch {
      message.error(`Unable to upload week ${weekNumber} material.`);
    } finally {
      setUploadingWeek(null);
    }
  }

  return (
    <Row className="institute-training-setup" gutter={[12, 12]} align="stretch">
      <Col xs={24} lg={12}>
        <Card title="Create training" style={{ height: '100%' }}>
          <Form layout="vertical" onFinish={(values) => void createTraining(values)} disabled={Boolean(createdTraining)}>
            <Form.Item name="title" label="Title" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="Description" rules={[{ required: true }]}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="startDate" label="Start date" rules={[{ required: true }]}>
              <Input type="date" />
            </Form.Item>
            <Form.Item name="endDate" label="End date" rules={[{ required: true }]}>
              <Input type="date" />
            </Form.Item>
            <Form.Item name="capacity" label="Capacity" rules={[{ required: true }]}>
              <InputNumber min={1} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="accessType" label="Access type" initialValue="PRIVATE" rules={[{ required: true }]}>
              <Select
                options={[
                  { value: 'PRIVATE', label: 'Private' },
                  { value: 'PUBLIC', label: 'Public' },
                  { value: 'STAFF', label: 'Staff' },
                ]}
              />
            </Form.Item>
            <Form.Item noStyle shouldUpdate={(previous, current) => previous.accessType !== current.accessType}>
              {({ getFieldValue }) =>
                getFieldValue('accessType') === 'STAFF' ? (
                  <Form.Item
                    name="staffAccessPassword"
                    label="Staff access password"
                    rules={[{ required: true, min: 8 }]}
                  >
                    <Input.Password />
                  </Form.Item>
                ) : null
              }
            </Form.Item>
            <Form.Item name="weeks" label="Number of weeks" initialValue={1} rules={[{ required: true }]}>
              <InputNumber min={1} max={52} onChange={(value) => setWeeks(Number(value ?? 1))} />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              {submitting ? 'Creating training...' : 'Create training'}
            </Button>
          </Form>
        </Card>
      </Col>
      <Col xs={24} lg={12}>
        <Card title="Upload course materials by week" style={{ height: '100%' }}>
          {!createdTraining ? (
            <Alert message="Create the training first to enable material uploads." type="info" showIcon />
          ) : createdTraining.accessType === 'PRIVATE' ? (
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Alert message="Upload the videos and documents for each week of this training." type="info" showIcon />
              {Array.from({ length: weeks }, (_, index) => index + 1).map((weekNumber) => (
                <Card size="small" title={`Week ${weekNumber}`} key={weekNumber}>
                  <Dragger
                    multiple
                    fileList={files[weekNumber] ?? []}
                    beforeUpload={() => false}
                    onChange={({ fileList }) => setFiles((current) => ({ ...current, [weekNumber]: fileList }))}
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
                    Upload week {weekNumber} materials
                  </Button>
                </Card>
              ))}
            </Space>
          ) : (
            <Alert
              message="Training materials can only be uploaded by the owning institute for private trainings."
              type="info"
              showIcon
            />
          )}
        </Card>
      </Col>
    </Row>
  );
}
