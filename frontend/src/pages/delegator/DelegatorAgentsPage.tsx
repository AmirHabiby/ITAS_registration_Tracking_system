import { Table } from "antd";

export function DelegatorAgentsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Representative", dataIndex: "representativeId" },
        { title: "Delegated At", dataIndex: "delegatedAt" },
      ]}
      rowKey="delegationId"
    />
  );
}
