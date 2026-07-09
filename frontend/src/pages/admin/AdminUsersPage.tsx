import { Table } from "antd";

export function AdminUsersPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Username", dataIndex: "username" },
        { title: "Role", dataIndex: "role" },
      ]}
      rowKey="username"
    />
  );
}
