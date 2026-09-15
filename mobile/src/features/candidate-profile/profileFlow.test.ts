import type { CandidateProfilePayload, CandidateProfileSaveResult } from './api/candidateProfileApi';
import { saveCandidateOnboarding, serializeSkills } from './profileFlow';

const payload: CandidateProfilePayload = {
  headline: 'Java Software Engineer',
  location: 'Murcia',
  bio: 'Backend engineer',
  linkedinUrl: null,
  githubUrl: 'https://github.example/candidate',
  portfolioUrl: null,
  skills: ['Java', 'Spring Boot'],
};

const created: CandidateProfileSaveResult = {
  status: 201,
  profile: {
    id: 'profile-id',
    ...payload,
    createdAt: '2026-09-15T00:00:00Z',
    updatedAt: '2026-09-15T00:00:00Z',
  },
};

describe('candidate profile flow', () => {
  it('serializes comma-separated skills for the API', () => {
    expect(serializeSkills(' Java, Spring Boot, , PostgreSQL ')).toEqual(['Java', 'Spring Boot', 'PostgreSQL']);
  });

  it('refreshes /auth/me state after a successful 201 onboarding save', async () => {
    const save = jest.fn().mockResolvedValue(created);
    const refreshSession = jest.fn().mockResolvedValue(undefined);

    await expect(saveCandidateOnboarding(payload, refreshSession, save)).resolves.toEqual(created);

    expect(save).toHaveBeenCalledWith(payload);
    expect(refreshSession).toHaveBeenCalledTimes(1);
  });

  it('does not refresh auth state when profile save fails', async () => {
    const save = jest.fn().mockRejectedValue(new Error('validation'));
    const refreshSession = jest.fn().mockResolvedValue(undefined);

    await expect(saveCandidateOnboarding(payload, refreshSession, save)).rejects.toThrow('validation');
    expect(refreshSession).not.toHaveBeenCalled();
  });
});
