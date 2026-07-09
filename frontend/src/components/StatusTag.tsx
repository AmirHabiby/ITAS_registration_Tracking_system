import { Tag } from "antd";

const colors: Record<string, string> = {
  PENDING: "gold",
  APPROVED: "green",
  REJECTED: "volcano",
  TRAINED: "blue",
  AGENT: "purple",
  COMPLETED: "green",
  FAILED: "red",
  RETAKE_REQUIRED: "orange",
  ENROLLED: "cyan",
  Ongoing: "cyan",
};

export function StatusTag({ value }: { value?: string | null }) {
  if (!value) {
    return null;
  }

  return <Tag color={colors[value] ?? "default"}>{value}</Tag>;
}
