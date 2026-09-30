/** The API's CreateEvalRunRequest.threshold is a fraction 0.0–1.0; the UI uses a percent. */
export function toApiThreshold(percent: number): number {
  const clamped = Math.min(100, Math.max(0, percent));
  return Math.round(clamped) / 100;
}
