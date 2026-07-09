import { Table } from "antd";

export function RepresentativeResultsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Training", dataIndex: "trainingId" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="enrollmentId"
    />
  );
}
