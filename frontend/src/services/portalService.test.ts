import { describe, expect, it, vi } from "vitest";
import { portalService } from "./portalService";
import { apiClient } from "./apiClient";

vi.mock("./apiClient", () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe("portalService", () => {
  it("uses the representative training endpoint", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] } as never);

    await portalService.listAvailableTrainings();

    expect(apiClient.get).toHaveBeenCalledWith("/api/representatives/me/trainings");
  });

  it("loads materials for a representative's approved training", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] } as never);

    await portalService.listRepresentativeTrainingMaterials("training-id");

    expect(apiClient.get).toHaveBeenCalledWith(
      "/api/representatives/me/trainings/training-id/materials",
    );
  });

  it("loads representative progress for a training", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: {} } as never);

    await portalService.getRepresentativeTrainingProgress("training-id");

    expect(apiClient.get).toHaveBeenCalledWith(
      "/api/representatives/me/trainings/training-id/materials/progress",
    );
  });

  it("marks a representative training material complete", async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} } as never);

    await portalService.completeRepresentativeTrainingMaterial("training-id", "material-id");

    expect(apiClient.post).toHaveBeenCalledWith(
      "/api/representatives/me/trainings/training-id/materials/material-id/complete",
    );
  });

  it("submits a representative training request", async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} } as never);

    await portalService.requestTraining("training-id", "representative-id");

    expect(apiClient.post).toHaveBeenCalledWith(
      "/api/representatives/me/training-requests",
      { trainingId: "training-id", representativeId: "representative-id" },
    );
  });

  it("deletes an institute training", async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined } as never);

    await portalService.deleteInstituteTraining("training-id");

    expect(apiClient.delete).toHaveBeenCalledWith("/api/institutes/me/trainings/training-id");
  });
});