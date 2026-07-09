import { Table } from "antd";

export function RepresentativeRequestsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Training", dataIndex: "trainingId" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="id"
    />
  );
}
