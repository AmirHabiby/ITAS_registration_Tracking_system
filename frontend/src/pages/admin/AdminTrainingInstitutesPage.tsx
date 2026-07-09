import { Table } from "antd";

export function AdminTrainingInstitutesPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Name", dataIndex: "name" },
        { title: "Contact Email", dataIndex: "contactEmail" },
      ]}
      rowKey="id"
    />
  );
}
