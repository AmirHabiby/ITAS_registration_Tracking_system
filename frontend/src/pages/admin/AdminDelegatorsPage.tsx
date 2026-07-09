import { Table } from "antd";

export function AdminDelegatorsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Name", dataIndex: "fullName" },
        { title: "Email", dataIndex: "email" },
      ]}
      rowKey="id"
    />
  );
}
