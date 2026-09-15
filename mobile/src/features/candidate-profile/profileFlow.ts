import {
  putCandidateProfile,
  type CandidateProfilePayload,
  type CandidateProfileSaveResult,
} from './api/candidateProfileApi';

export function serializeSkills(value: string): string[] {
  return value
    .split(',')
    .map((skill) => skill.trim())
    .filter((skill) => skill.length > 0);
}

export async function saveCandidateOnboarding(
  payload: CandidateProfilePayload,
  refreshSession: () => Promise<void>,
  save: (payload: CandidateProfilePayload) => Promise<CandidateProfileSaveResult> = putCandidateProfile,
): Promise<CandidateProfileSaveResult> {
  const result = await save(payload);
  await refreshSession();
  return result;
}
