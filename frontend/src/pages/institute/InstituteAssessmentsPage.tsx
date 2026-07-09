import { Table } from "antd";

export function InstituteAssessmentsPage() {
  return (
    <Table
      dataSource={[]}
      columns={[
        { title: "Enrollment", dataIndex: "trainingEnrollmentId" },
        { title: "Score", dataIndex: "score" },
      ]}
      rowKey="id"
    />
  );
}
