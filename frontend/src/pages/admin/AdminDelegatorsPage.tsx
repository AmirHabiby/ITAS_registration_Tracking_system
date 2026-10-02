import { Card } from "antd";
import { AdminCreateForm } from "../../components/AdminCreateForm";
import { portalService } from "../../services/portalService";

export function AdminDelegatorsPage() {
  return (
    <Card title="Create delegator">
      <AdminCreateForm kind="delegator" onSubmit={async (body, image) => { await portalService.createDelegator(body, image); }} />
    </Card>
  );
}
