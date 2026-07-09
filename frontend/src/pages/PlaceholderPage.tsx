import { Card, Typography } from "antd";

export function PlaceholderPage({
  title,
  subtitle,
}: {
  title: string;
  subtitle: string;
}) {
  return (
    <Card>
      <Typography.Title level={3}>{title}</Typography.Title>
      <Typography.Paragraph>{subtitle}</Typography.Paragraph>
    </Card>
  );
}
