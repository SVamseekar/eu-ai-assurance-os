/** Demo/mock entity ids used only when the API is offline. */
export function isMockEntityId(id: string | null | undefined): boolean {
  if (!id) return true;
  return id.startsWith("mock-");
}

export function isLiveEntityId(id: string | null | undefined): id is string {
  return Boolean(id) && !isMockEntityId(id);
}
