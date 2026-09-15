import type {
  CompanyPayload,
  CompanyResponse,
  RecruiterProfilePayload,
  RecruiterProfileSaveResult,
} from './api/recruiterProfileApi';
import { saveCompanyEdit, saveRecruiterOnboarding } from './profileFlow';

const payload: RecruiterProfilePayload = {
  position: 'Technical Recruiter',
  company: {
    name: 'Example Company',
    description: 'Technology company',
    website: 'https://example.test',
    logoUrl: null,
    location: 'Murcia',
  },
};

const created: RecruiterProfileSaveResult = {
  status: 201,
  profile: {
    id: 'recruiter-profile-id',
    position: payload.position,
    companyId: 'company-id',
    createdAt: '2026-09-15T00:00:00Z',
    updatedAt: '2026-09-15T00:00:00Z',
  },
};

describe('recruiter profile flow', () => {
  it('serializes the onboarding payload with its nested company', () => {
    expect(payload).toEqual({
      position: 'Technical Recruiter',
      company: {
        name: 'Example Company',
        description: 'Technology company',
        website: 'https://example.test',
        logoUrl: null,
        location: 'Murcia',
      },
    });
  });

  it('refreshes /auth/me after successful onboarding', async () => {
    const save = jest.fn().mockResolvedValue(created);
    const refreshSession = jest.fn().mockResolvedValue(undefined);

    await expect(saveRecruiterOnboarding(payload, refreshSession, save)).resolves.toEqual(created);

    expect(save).toHaveBeenCalledWith(payload);
    expect(refreshSession).toHaveBeenCalledTimes(1);
  });

  it('saves a full company replacement payload', async () => {
    const companyPayload: CompanyPayload = {
      name: 'Example Updated',
      description: 'Updated company',
      website: null,
      logoUrl: null,
      location: 'Madrid',
    };
    const response: CompanyResponse = {
      id: 'company-id',
      ...companyPayload,
      createdAt: '2026-09-15T00:00:00Z',
      updatedAt: '2026-09-15T01:00:00Z',
    };
    const save = jest.fn().mockResolvedValue(response);

    await expect(saveCompanyEdit(companyPayload, save)).resolves.toEqual(response);
    expect(save).toHaveBeenCalledWith(companyPayload);
  });
});
