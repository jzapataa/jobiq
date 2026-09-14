import type { AuthUser } from '../../../types/auth';

export type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated' | 'error';

export interface AuthStateSnapshot {
  status: AuthStatus;
  user: AuthUser | null;
  accessToken: string | null;
  bootstrapError: string | null;
}

export function createInitialAuthState(): AuthStateSnapshot {
  return {
    status: 'checking',
    user: null,
    accessToken: null,
    bootstrapError: null,
  };
}
