import axios from 'axios';

import { appConfig } from '../config/env';
import { toApiError } from './apiError';
import { notifyUnauthorized, shouldInvalidateSession } from './unauthorizedHandler';

let accessToken: string | null = null;

export function setApiAccessToken(token: string | null): void {
  accessToken = token;
}

export const apiClient = axios.create({
  baseURL: appConfig.apiUrl || undefined,
  timeout: appConfig.apiTimeoutMs,
});

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.set('Authorization', `Bearer ${accessToken}`);
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    const apiError = toApiError(error);
    if (shouldInvalidateSession(apiError.status, accessToken !== null)) {
      await notifyUnauthorized();
    }
    return Promise.reject(apiError);
  },
);
