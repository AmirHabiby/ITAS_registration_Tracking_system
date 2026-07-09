import { Table } from "antd";

export function InstituteTrainingsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Title", dataIndex: "title" },
        { title: "Status", dataIndex: "status" },
      ]}
      rowKey="id"
    />
  );
}
