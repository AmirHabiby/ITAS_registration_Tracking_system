import { describe, expect, it, vi } from "vitest";
import { portalService } from "./portalService";
import { apiClient } from "./apiClient";

vi.mock("./apiClient", () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}));

describe("portalService", () => {
  it("uses the representative training endpoint", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] } as never);

    await portalService.listAvailableTrainings();

    expect(apiClient.get).toHaveBeenCalledWith("/api/representatives/me/trainings");
  });

  it("submits a representative training request", async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: {} } as never);

    await portalService.requestTraining("training-id", "representative-id");

    expect(apiClient.post).toHaveBeenCalledWith(
      "/api/representatives/me/training-requests",
      { trainingId: "training-id", representativeId: "representative-id" },
    );
  });
});