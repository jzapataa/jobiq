import axios from 'axios';

import { appConfig } from '@/core/config/env';

export const apiClient = axios.create({
  baseURL: appConfig.apiUrl || undefined,
  timeout: appConfig.apiTimeoutMs,
});
