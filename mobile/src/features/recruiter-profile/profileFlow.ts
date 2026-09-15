import {
  putRecruiterCompany,
  putRecruiterProfile,
  type CompanyPayload,
  type CompanyResponse,
  type RecruiterProfilePayload,
  type RecruiterProfileSaveResult,
} from './api/recruiterProfileApi';

export async function saveRecruiterOnboarding(
  payload: RecruiterProfilePayload,
  refreshSession: () => Promise<void>,
  save: (payload: RecruiterProfilePayload) => Promise<RecruiterProfileSaveResult> = putRecruiterProfile,
): Promise<RecruiterProfileSaveResult> {
  const result = await save(payload);
  await refreshSession();
  return result;
}

export async function saveCompanyEdit(
  payload: CompanyPayload,
  save: (payload: CompanyPayload) => Promise<CompanyResponse> = putRecruiterCompany,
): Promise<CompanyResponse> {
  return save(payload);
}
