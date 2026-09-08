import { Alert, Button, Input, Modal, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { portalService, type TrainingRequest } from "../../services/portalService";

export function DelegatorTrainingRequestsPage() {
  const [requests, setRequests] = useState<TrainingRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reviewing, setReviewing] = useState<{ id: string; action: "approve" | "reject" } | null>(null);
  const [note, setNote] = useState("");
  const [decisionLoading, setDecisionLoading] = useState(false);

  async function loadRequests() {
    try {
      setError(null);
      setRequests((await portalService.listDelegatorRequests()).data);
    } catch {
      setError("Unable to load training requests.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadRequests(); }, []);

  async function decide() {
    if (!reviewing) return;
    setDecisionLoading(true);
    try {
      await portalService.decideDelegatorRequest(reviewing.id, reviewing.action, note);
      message.success(`Request ${reviewing.action === "approve" ? "approved" : "rejected"}.`);
      setReviewing(null);
      setNote("");
      await loadRequests();
    } catch {
      message.error("Unable to update the request.");
    } finally {
      setDecisionLoading(false);
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={requests} rowKey="id" columns={[
        { title: "Representative", dataIndex: "representativeName", render: (value: string | undefined, request) => value ?? request.representativeId },
        { title: "Training", dataIndex: "trainingTitle", render: (value: string | undefined, request) => value ?? request.trainingId },
        { title: "Requested", dataIndex: "requestedAt", render: (value: string) => new Date(value).toLocaleString() },
        { title: "Status", render: (_, request) => <Tag>{request.status}</Tag> },
        {
          title: "Actions",
          render: (_, request) => request.status === "PENDING" && <Space>
            <Button type="primary" onClick={() => setReviewing({ id: request.id, action: "approve" })}>Approve</Button>
            <Button danger onClick={() => setReviewing({ id: request.id, action: "reject" })}>Reject</Button>
          </Space>,
        },
      ]} />
      <Modal open={reviewing !== null} confirmLoading={decisionLoading} title={reviewing?.action === "approve" ? "Approve request" : "Reject request"} onOk={() => void decide()} onCancel={() => !decisionLoading && setReviewing(null)}>
        <Input.TextArea value={note} onChange={(event) => setNote(event.target.value)} placeholder="Add a note" rows={4} />
      </Modal>
    </Space>
  );
}
