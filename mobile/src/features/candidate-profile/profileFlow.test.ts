import { ApiError } from '../../core/api/apiError';
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

  it('propagates validation errors and does not refresh auth state', async () => {
    const validationError = new ApiError(
      400,
      'VALIDATION_ERROR',
      'Request validation failed',
      null,
      [{ field: 'headline', message: 'must not be blank' }],
    );
    const save = jest.fn().mockRejectedValue(validationError);
    const refreshSession = jest.fn().mockResolvedValue(undefined);

    await expect(saveCandidateOnboarding(payload, refreshSession, save)).rejects.toMatchObject({
      status: 400,
      code: 'VALIDATION_ERROR',
    });
    expect(refreshSession).not.toHaveBeenCalled();
  });
});
