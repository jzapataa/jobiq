import { apiClient } from '../../../core/api/apiClient';

export interface CompanyPayload {
  name: string;
  description: string;
  website: string | null;
  logoUrl: string | null;
  location: string;
}

export interface CompanyResponse extends CompanyPayload {
  id: string;
  createdAt: string;
  updatedAt: string;
}

export interface RecruiterProfilePayload {
  position: string;
  company?: CompanyPayload;
}

export interface RecruiterProfileResponse {
  id: string;
  position: string;
  companyId: string;
  createdAt: string;
  updatedAt: string;
}

export interface RecruiterProfileSaveResult {
  status: 200 | 201;
  profile: RecruiterProfileResponse;
}

export async function getRecruiterProfile(): Promise<RecruiterProfileResponse> {
  const response = await apiClient.get<RecruiterProfileResponse>('/api/v1/recruiter/profile');
  return response.data;
}

export async function putRecruiterProfile(payload: RecruiterProfilePayload): Promise<RecruiterProfileSaveResult> {
  const response = await apiClient.put<RecruiterProfileResponse>('/api/v1/recruiter/profile', payload);
  return { status: response.status as 200 | 201, profile: response.data };
}

export async function getRecruiterCompany(): Promise<CompanyResponse> {
  const response = await apiClient.get<CompanyResponse>('/api/v1/recruiter/company');
  return response.data;
}

export async function putRecruiterCompany(payload: CompanyPayload): Promise<CompanyResponse> {
  const response = await apiClient.put<CompanyResponse>('/api/v1/recruiter/company', payload);
  return response.data;
}
