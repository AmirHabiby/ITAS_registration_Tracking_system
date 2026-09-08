import { Card } from "antd";
import { AdminCreateForm } from "../../components/AdminCreateForm";
import { portalService } from "../../services/portalService";

export function AdminTrainingInstitutesPage() {
  return (
    <Card title="Create training institute">
      <AdminCreateForm kind="institute" onSubmit={async (body) => { await portalService.createInstitute(body); }} />
    </Card>
  );
}
