import type { AuthUser } from '../../../types/auth';
import { resolveAuthRoute } from './authRoute';

function user(role: AuthUser['role'], profileComplete: boolean): AuthUser {
  return {
    id: `${role.toLowerCase()}-id`,
    email: `${role.toLowerCase()}@example.com`,
    name: role,
    role,
    profileComplete,
  };
}

describe('auth routing', () => {
  it('routes unauthenticated users to public login', () => {
    expect(resolveAuthRoute('unauthenticated', null)).toBe('/(public)/login');
  });

  it('routes an incomplete Candidate to onboarding and a completed Candidate to its shell', () => {
    expect(resolveAuthRoute('authenticated', user('CANDIDATE', false))).toBe('/(candidate)/onboarding');
    expect(resolveAuthRoute('authenticated', user('CANDIDATE', true))).toBe('/(candidate)/candidate');
  });

  it('routes an incomplete Recruiter to onboarding and a completed Recruiter to its shell', () => {
    expect(resolveAuthRoute('authenticated', user('RECRUITER', false))).toBe('/(recruiter)/onboarding');
    expect(resolveAuthRoute('authenticated', user('RECRUITER', true))).toBe('/(recruiter)/recruiter');
  });

  it('does not route while checking or after a recoverable bootstrap error', () => {
    expect(resolveAuthRoute('checking', null)).toBeNull();
    expect(resolveAuthRoute('error', null)).toBeNull();
  });
});
