import { create } from 'zustand';

import { setApiAccessToken } from '../../../core/api/apiClient';
import { ApiError, toApiError } from '../../../core/api/apiError';
import { setUnauthorizedHandler } from '../../../core/api/unauthorizedHandler';
import {
  clearStoredAccessToken,
  readStoredAccessToken,
  writeStoredAccessToken,
} from '../../../core/storage/authTokenStorage';
import { getCurrentUser, loginRequest } from '../api/authApi';
import { createInitialAuthState, type AuthStateSnapshot } from './authState';

interface AuthStore extends AuthStateSnapshot {
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  restoreSession: () => Promise<void>;
  invalidateSession: () => Promise<void>;
}

function errorMessage(error: unknown): string {
  return toApiError(error).message;
}

export const useAuthStore = create<AuthStore>()((set, get) => ({
  ...createInitialAuthState(),

  login: async (email, password) => {
    set({ status: 'checking', user: null, bootstrapError: null });

    try {
      const tokenResponse = await loginRequest(email, password);
      await writeStoredAccessToken(tokenResponse.accessToken);
      setApiAccessToken(tokenResponse.accessToken);
      set({ accessToken: tokenResponse.accessToken });

      const user = await getCurrentUser();
      set({ status: 'authenticated', user, bootstrapError: null });
    } catch (error) {
      const apiError = error instanceof ApiError ? error : toApiError(error);
      if (apiError.status === 401 && get().accessToken) {
        await get().invalidateSession();
      } else if (get().accessToken) {
        set({ status: 'error', user: null, bootstrapError: apiError.message });
      } else {
        set({ status: 'unauthenticated', user: null, bootstrapError: null });
      }
      throw apiError;
    }
  },

  logout: async () => {
    try {
      await clearStoredAccessToken();
    } finally {
      setApiAccessToken(null);
      set({ status: 'unauthenticated', user: null, accessToken: null, bootstrapError: null });
    }
  },

  restoreSession: async () => {
    set({ status: 'checking', user: null, bootstrapError: null });

    try {
      const token = await readStoredAccessToken();
      if (!token) {
        setApiAccessToken(null);
        set({ status: 'unauthenticated', accessToken: null, user: null, bootstrapError: null });
        return;
      }

      setApiAccessToken(token);
      set({ accessToken: token });

      const user = await getCurrentUser();
      set({ status: 'authenticated', user, bootstrapError: null });
    } catch (error) {
      const apiError = error instanceof ApiError ? error : toApiError(error);
      if (apiError.status === 401) {
        await get().invalidateSession();
        return;
      }

      set({ status: 'error', user: null, bootstrapError: errorMessage(apiError) });
    }
  },

  invalidateSession: async () => {
    try {
      await clearStoredAccessToken();
    } finally {
      setApiAccessToken(null);
      set({ status: 'unauthenticated', user: null, accessToken: null, bootstrapError: null });
    }
  },
}));

setUnauthorizedHandler(() => useAuthStore.getState().invalidateSession());
