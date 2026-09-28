import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "./apiClient";
import { assessmentService } from "./assessmentService";

vi.mock("./apiClient", () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe("assessmentService", () => {
  beforeEach(() => vi.resetAllMocks());

  it("loads the institute-owned assessment list for a selected course", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] } as never);

    await assessmentService.listInstituteAssessments("course-123");

    expect(apiClient.get).toHaveBeenCalledWith(
      "/api/institutes/me/trainings/course-123/online-assessments",
    );
  });

  it("saves a representative response using the attempt answer API", async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ data: {} } as never);

    await assessmentService.saveAnswer("attempt-1", "question-2", {
      selectedOptionId: "option-3",
      responseText: null,
    });

    expect(apiClient.put).toHaveBeenCalledWith(
      "/api/representative/online-assessment-attempts/attempt-1/answers/question-2",
      { selectedOptionId: "option-3", responseText: null },
    );
  });

  it("loads released assessment outcomes through the delegator API", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: [] } as never);

    await assessmentService.listDelegatorOutcomes();

    expect(apiClient.get).toHaveBeenCalledWith("/api/delegators/me/assessment-outcomes");
  });
});
