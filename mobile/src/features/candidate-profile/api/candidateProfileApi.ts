import { apiClient } from '../../../core/api/apiClient';

export interface CandidateProfilePayload {
  headline: string;
  location: string;
  bio: string;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  skills: string[];
}

export interface CandidateProfileResponse extends CandidateProfilePayload {
  id: string;
  createdAt: string;
  updatedAt: string;
}

export interface CandidateProfileSaveResult {
  status: 200 | 201;
  profile: CandidateProfileResponse;
}

export async function getCandidateProfile(): Promise<CandidateProfileResponse> {
  const response = await apiClient.get<CandidateProfileResponse>('/api/v1/candidate/profile');
  return response.data;
}

export async function putCandidateProfile(payload: CandidateProfilePayload): Promise<CandidateProfileSaveResult> {
  const response = await apiClient.put<CandidateProfileResponse>('/api/v1/candidate/profile', payload);
  return {
    status: response.status as 200 | 201,
    profile: response.data,
  };
}
