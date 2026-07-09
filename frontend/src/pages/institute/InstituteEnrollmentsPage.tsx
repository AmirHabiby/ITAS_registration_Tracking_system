import { Table } from "antd";

export function InstituteEnrollmentsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Representative", dataIndex: "representativeId" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="id"
    />
  );
}
