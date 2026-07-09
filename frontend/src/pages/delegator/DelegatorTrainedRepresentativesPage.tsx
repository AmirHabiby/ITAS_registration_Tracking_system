import { Table } from "antd";

export function DelegatorTrainedRepresentativesPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Name", dataIndex: "fullName" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="id"
    />
  );
}
