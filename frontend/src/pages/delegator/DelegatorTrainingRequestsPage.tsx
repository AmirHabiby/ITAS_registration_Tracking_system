import { Table } from "antd";

export function DelegatorTrainingRequestsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Request", dataIndex: "id" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="id"
    />
  );
}
