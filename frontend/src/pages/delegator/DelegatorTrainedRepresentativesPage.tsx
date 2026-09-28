import { Alert, Button, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type DelegatorTrainedOption } from "../../services/portalService";

export function DelegatorTrainedRepresentativesPage() {
  const [options, setOptions] = useState<DelegatorTrainedOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [assigningId, setAssigningId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadRepresentatives() {
    try {
      setError(null);
      setOptions((await portalService.listTrainedRepresentatives()).data);
    } catch {
      setError("Unable to load trained representatives.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadRepresentatives(); }, []);

  async function assignOption(option: DelegatorTrainedOption) {
    setAssigningId(`${option.type}-${option.id}`);
    try {
      if (option.type === "FIRM") {
        await portalService.delegateFirm(option.id, "Firm assigned as agent");
        message.success("Firm assigned as an agent.");
      } else {
        await portalService.markRepresentativeAsAgent(option.id, "Marked as agent");
        message.success("Representative marked as an agent.");
      }
      await loadRepresentatives();
    } catch {
      message.error("Unable to mark representative as an agent.");
    } finally {
      setAssigningId(null);
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={options} rowKey={(option) => `${option.type}-${option.id}`} columns={[
        { title: "Name", dataIndex: "name" },
        { title: "Email", dataIndex: "email" },
        { title: "Type", render: (_, option) => <Tag color={option.type === "FIRM" ? "blue" : "default"}>{option.type === "FIRM" ? "Firm" : "Individual"}</Tag> },
        { title: "Trained staff", render: (_, option) => option.type === "FIRM" ? option.trainedStaffCount : "-" },
        { title: "Status", render: (_, option) => <Tag>{option.status}</Tag> },
        { title: "Action", render: (_, option) => {
          const optionId = `${option.type}-${option.id}`;
          return <Button type="primary" loading={assigningId === optionId} disabled={option.delegated || assigningId !== null} onClick={() => void assignOption(option)}>
            {option.delegated ? "Already assigned" : assigningId === optionId ? "Assigning..." : option.type === "FIRM" ? "Assign firm as agent" : "Mark as agent"}
          </Button>;
        } },
      ]} />
    </Space>
  );
}
