import { Alert, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { portalService, type Training } from '../../services/portalService';

export function InstituteTrainingsPage() {
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void portalService.listInstituteTrainings()
      .then((response) => setTrainings(response.data))
      .catch(() => setError('Unable to load institute trainings.'))
      .finally(() => setLoading(false));
  }, []);

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
        ]}
      />
    </>
  );
}
