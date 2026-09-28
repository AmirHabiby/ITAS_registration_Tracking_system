import { describe, expect, it } from "vitest";
import { formatCountdown } from "./assessmentUtils";

describe("formatCountdown", () => {
  it("formats minute and second countdowns", () => {
    expect(formatCountdown(3599)).toBe("59:59");
    expect(formatCountdown(61)).toBe("01:01");
    expect(formatCountdown(0)).toBe("00:00");
  });

  it("includes hours for longer assessments and clamps negative values", () => {
    expect(formatCountdown(3661)).toBe("01:01:01");
    expect(formatCountdown(-12)).toBe("00:00");
  });
});
