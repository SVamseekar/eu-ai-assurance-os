/** Fired on window when the API answers 402; detail is the human-readable limit message. */
export const PLAN_LIMIT_EVENT = "aos:plan-limit";

export function dispatchPlanLimit(message: string): void {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new CustomEvent(PLAN_LIMIT_EVENT, { detail: message }));
}

/**
 * For raw `fetch` calls (downloads) that bypass the shared request helper: on a 402 reads the API's message,
 * opens the upgrade prompt, and returns that message so the caller can throw it.
 */
export async function reportPlanLimit(res: Response): Promise<string | null> {
  if (res.status !== 402) return null;
  let message = "Your plan does not include this.";
  try {
    const body = await res.clone().json();
    message = body.message ?? message;
  } catch {
    // keep the default message
  }
  dispatchPlanLimit(message);
  return message;
}
