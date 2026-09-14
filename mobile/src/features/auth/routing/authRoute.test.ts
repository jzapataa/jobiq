import type { AuthUser } from '../../../types/auth';
import { resolveAuthRoute } from './authRoute';

const candidate: AuthUser = {
  id: 'candidate-id',
  email: 'candidate@example.com',
  name: 'Candidate',
  role: 'CANDIDATE',
  profileComplete: false,
};

const recruiter: AuthUser = {
  ...candidate,
  id: 'recruiter-id',
  email: 'recruiter@example.com',
  name: 'Recruiter',
  role: 'RECRUITER',
};

describe('auth routing', () => {
  it('routes unauthenticated users to public login', () => {
    expect(resolveAuthRoute('unauthenticated', null)).toBe('/(public)/login');
  });

  it('routes authenticated candidates to the candidate shell', () => {
    expect(resolveAuthRoute('authenticated', candidate)).toBe('/(candidate)/candidate');
  });

  it('routes authenticated recruiters to the recruiter shell', () => {
    expect(resolveAuthRoute('authenticated', recruiter)).toBe('/(recruiter)/recruiter');
  });

  it('does not route while checking or after a recoverable bootstrap error', () => {
    expect(resolveAuthRoute('checking', null)).toBeNull();
    expect(resolveAuthRoute('error', null)).toBeNull();
  });
});
