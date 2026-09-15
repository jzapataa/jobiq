import type { AuthStatus } from '../store/authState';
import type { AuthUser } from '../../../types/auth';

export type AuthRoute = '/(public)/login' | '/(candidate)/candidate' | '/(recruiter)/recruiter' | null;

export function resolveAuthRoute(status: AuthStatus, user: AuthUser | null): AuthRoute {
  if (status === 'unauthenticated') {
    return '/(public)/login';
  }

  if (status === 'authenticated' && user?.role === 'CANDIDATE') {
    return '/(candidate)/candidate';
  }

  if (status === 'authenticated' && user?.role === 'RECRUITER') {
    return '/(recruiter)/recruiter';
  }

  return null;
}
