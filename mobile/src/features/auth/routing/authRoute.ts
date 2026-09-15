import type { AuthStatus } from '../store/authState';
import type { AuthUser } from '../../../types/auth';

export type AuthRoute =
  | '/(public)/login'
  | '/(candidate)/onboarding'
  | '/(candidate)/candidate'
  | '/(recruiter)/onboarding'
  | '/(recruiter)/recruiter'
  | null;

export function resolveAuthRoute(status: AuthStatus, user: AuthUser | null): AuthRoute {
  if (status === 'unauthenticated') {
    return '/(public)/login';
  }

  if (status === 'authenticated' && user?.role === 'CANDIDATE') {
    return user.profileComplete ? '/(candidate)/candidate' : '/(candidate)/onboarding';
  }

  if (status === 'authenticated' && user?.role === 'RECRUITER') {
    return user.profileComplete ? '/(recruiter)/recruiter' : '/(recruiter)/onboarding';
  }

  return null;
}
