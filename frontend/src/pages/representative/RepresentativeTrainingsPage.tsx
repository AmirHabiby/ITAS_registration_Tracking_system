import { Alert, Button, Space, Table, Tag, message } from "antd";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { portalService, type Training } from "../../services/portalService";

export function RepresentativeTrainingsPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [trainings, setTrainings] = useState<Training[]>([]);
  const [requestStatuses, setRequestStatuses] = useState<Record<string, string>>({});
  const [requestingId, setRequestingId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadTrainings() {
    try {
      setError(null);
      const [trainingResponse, requestResponse] = await Promise.all([
        portalService.listAvailableTrainings(),
        portalService.listMyRequests(),
      ]);
      setTrainings(trainingResponse.data);
      setRequestStatuses(Object.fromEntries(requestResponse.data.map((request) => [request.trainingId, request.status])));
    } catch {
      setError("Unable to load available trainings.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadTrainings(); }, []);

  async function requestTraining(trainingId: string) {
    if (!user?.id) return;
    setRequestingId(trainingId);
    try {
      const response = await portalService.requestTraining(trainingId, user.id);
      setRequestStatuses((current) => ({ ...current, [trainingId]: response.data.status || "PENDING" }));
      message.success("Training request submitted.");
    } catch {
      message.error("Unable to submit the training request.");
    } finally {
      setRequestingId(null);
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      {error && <Alert message={error} type="error" showIcon />}
      <Table loading={loading} dataSource={trainings} rowKey="id" onRow={(training) => ({
        onClick: () => {
          if (requestStatuses[training.id] === "APPROVED") {
            navigate(`/representative/trainings/${training.id}/materials`);
          }
        },
        style: { cursor: requestStatuses[training.id] === "APPROVED" ? "pointer" : "default" },
      })} columns={[
        { title: "Title", dataIndex: "title" },
        { title: "Description", dataIndex: "description" },
        { title: "Dates", render: (_, training) => `${training.startDate} - ${training.endDate}` },
        { title: "Capacity", dataIndex: "capacity" },
        { title: "Access", dataIndex: "accessType", render: (accessType: Training["accessType"]) => <Tag>{accessType}</Tag> },
        { title: "Status", render: (_, training) => <Tag color={training.active ? "green" : "default"}>{training.status}</Tag> },
        { title: "Action", render: (_, training) => {
          const requestStatus = requestStatuses[training.id];
          const pending = requestStatus === "PENDING";
          const approved = requestStatus === "APPROVED";
          const rejected = requestStatus === "REJECTED";
          return <Button
            type="primary"
            loading={requestingId === training.id}
            onClick={(event) => {
              event.stopPropagation();
              if (approved) {
                navigate(`/representative/trainings/${training.id}/materials`);
              } else {
                void requestTraining(training.id);
              }
            }}
            disabled={approved ? false : !training.active || pending || requestingId !== null}
          >
            {requestingId === training.id ? "Requesting..." : approved ? "View materials" : pending ? "Requested" : rejected ? "Request again" : !training.active ? "Unavailable" : "Request training"}
          </Button>;
        } },
      ]} />
    </Space>
  );
}
