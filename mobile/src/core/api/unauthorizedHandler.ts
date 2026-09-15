export type UnauthorizedHandler = () => void | Promise<void>;

let unauthorizedHandler: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(handler: UnauthorizedHandler): void {
  unauthorizedHandler = handler;
}

export function shouldInvalidateSession(status: number | null, hasAccessToken: boolean): boolean {
  return status === 401 && hasAccessToken;
}

export async function notifyUnauthorized(): Promise<void> {
  await unauthorizedHandler?.();
}
