import { DeleteOutlined } from '@ant-design/icons';
import { Alert, Button, Popconfirm, Table, Tag, message } from 'antd';
import { useEffect, useState } from 'react';
import { portalService, type Training } from '../../services/portalService';

export function InstituteTrainingsPage() {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingTrainingId, setDeletingTrainingId] = useState<string | null>(null);

  useEffect(() => {
    void portalService.listInstituteTrainings()
      .then((response) => setTrainings(response.data))
      .catch(() => setError('Unable to load institute trainings.'))
      .finally(() => setLoading(false));
  }, []);

  async function deleteTraining(training: Training) {
    setDeletingTrainingId(training.id);
    try {
      await portalService.deleteInstituteTraining(training.id);
      setTrainings((current) => current.filter((item) => item.id !== training.id));
      message.success(`"${training.title}" and its uploaded materials were deleted.`);
    } catch (deleteError: unknown) {
      const responseData = typeof deleteError === 'object' && deleteError !== null && 'response' in deleteError
        ? (deleteError as { response?: { data?: { message?: string; detail?: string } } }).response?.data
        : undefined;
      message.error(
        responseData?.message
          ?? responseData?.detail
          ?? `Unable to delete "${training.title}". Please try again.`,
      );
    } finally {
      setDeletingTrainingId(null);
    }
  }

  return (
    <>
      {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />}
      <Table
        loading={loading}
        dataSource={trainings}
        rowKey="id"
        columns={[
          { title: 'Title', dataIndex: 'title' },
          { title: 'Dates', render: (_, training) => `${training.startDate} - ${training.endDate}` },
          { title: 'Capacity', dataIndex: 'capacity' },
          {
            title: 'Access',
            dataIndex: 'accessType',
            render: (accessType: Training['accessType']) => <Tag>{accessType}</Tag>,
          },
          {
            title: 'Status',
            render: (_, training) => <Tag color={training.active ? 'green' : 'default'}>{training.status}</Tag>,
          },
          {
            title: 'Action',
            key: 'action',
            align: 'right',
            render: (_, training) => (
              <Popconfirm
                rootClassName="institute-training-delete-confirm"
                title="Delete this training?"
                description="This permanently deletes the training, uploaded materials, enrollments, and assessments."
                okText="Delete"
                okButtonProps={{ danger: true, loading: deletingTrainingId === training.id }}
                cancelText="Cancel"
                onConfirm={() => deleteTraining(training)}
              >
                <Button
                  danger
                  type="text"
                  aria-label={`Delete ${training.title}`}
                  title={`Delete ${training.title}`}
                  icon={<DeleteOutlined />}
                  loading={deletingTrainingId === training.id}
                  disabled={deletingTrainingId !== null && deletingTrainingId !== training.id}
                  onClick={(event) => event.stopPropagation()}
                />
              </Popconfirm>
            ),
          },
        ]}
      />
    </>
  );
}
