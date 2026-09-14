import { apiClient } from '../../../core/api/apiClient';
import type { AuthUser, UserRole } from '../../../types/auth';

export interface LoginTokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  name: string;
  role: UserRole;
}

export async function loginRequest(email: string, password: string): Promise<LoginTokenResponse> {
  const response = await apiClient.post<LoginTokenResponse>('/api/v1/auth/login', { email, password });
  return response.data;
}

export async function registerRequest(payload: RegisterPayload): Promise<void> {
  await apiClient.post('/api/v1/auth/register', payload);
}

export async function getCurrentUser(): Promise<AuthUser> {
  const response = await apiClient.get<AuthUser>('/api/v1/auth/me');
  return response.data;
}
