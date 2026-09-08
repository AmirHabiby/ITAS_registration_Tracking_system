import { Card } from "antd";
import { AdminCreateForm } from "../../components/AdminCreateForm";
import { portalService } from "../../services/portalService";

export function AdminRepresentativesPage() {
  return (
    <Card title="Create representative">
      <AdminCreateForm kind="representative" onSubmit={async (body) => { await portalService.createRepresentative(body); }} />
    </Card>
  );
}
