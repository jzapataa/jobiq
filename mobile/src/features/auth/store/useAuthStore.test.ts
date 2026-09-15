import { ApiError } from '../../../core/api/apiError';
import { setApiAccessToken } from '../../../core/api/apiClient';
import {
  clearStoredAccessToken,
  readStoredAccessToken,
  writeStoredAccessToken,
} from '../../../core/storage/authTokenStorage';
import type { AuthUser } from '../../../types/auth';
import { getCurrentUser, loginRequest } from '../api/authApi';
import { createInitialAuthState } from './authState';
import { useAuthStore } from './useAuthStore';

jest.mock('../../../core/api/apiClient', () => ({
  setApiAccessToken: jest.fn(),
}));

jest.mock('../../../core/storage/authTokenStorage', () => ({
  readStoredAccessToken: jest.fn(),
  writeStoredAccessToken: jest.fn(),
  clearStoredAccessToken: jest.fn(),
}));

jest.mock('../api/authApi', () => ({
  loginRequest: jest.fn(),
  getCurrentUser: jest.fn(),
}));

const mockedReadToken = jest.mocked(readStoredAccessToken);
const mockedWriteToken = jest.mocked(writeStoredAccessToken);
const mockedClearToken = jest.mocked(clearStoredAccessToken);
const mockedSetApiAccessToken = jest.mocked(setApiAccessToken);
const mockedLogin = jest.mocked(loginRequest);
const mockedGetCurrentUser = jest.mocked(getCurrentUser);

const candidate: AuthUser = {
  id: 'candidate-id',
  email: 'candidate@example.com',
  name: 'Candidate',
  role: 'CANDIDATE',
  profileComplete: false,
};

beforeEach(() => {
  jest.clearAllMocks();
  useAuthStore.setState(createInitialAuthState());
});

describe('auth store', () => {
  it('restores to unauthenticated when SecureStore has no token', async () => {
    mockedReadToken.mockResolvedValue(null);

    await useAuthStore.getState().restoreSession();

    expect(useAuthStore.getState().status).toBe('unauthenticated');
    expect(useAuthStore.getState().accessToken).toBeNull();
    expect(mockedSetApiAccessToken).toHaveBeenCalledWith(null);
  });

  it('restores a valid SecureStore session through /auth/me', async () => {
    mockedReadToken.mockResolvedValue('stored-token');
    mockedGetCurrentUser.mockResolvedValue(candidate);

    await useAuthStore.getState().restoreSession();

    expect(mockedSetApiAccessToken).toHaveBeenCalledWith('stored-token');
    expect(useAuthStore.getState()).toMatchObject({
      status: 'authenticated',
      accessToken: 'stored-token',
      user: candidate,
    });
  });

  it('clears an invalid session when /auth/me returns 401', async () => {
    mockedReadToken.mockResolvedValue('expired-token');
    mockedGetCurrentUser.mockRejectedValue(new ApiError(401, 'UNAUTHENTICATED', 'Authentication required'));

    await useAuthStore.getState().restoreSession();

    expect(mockedClearToken).toHaveBeenCalledTimes(1);
    expect(mockedSetApiAccessToken).toHaveBeenLastCalledWith(null);
    expect(useAuthStore.getState()).toMatchObject({
      status: 'unauthenticated',
      accessToken: null,
      user: null,
    });
  });

  it('does not invalidate the token on 403', async () => {
    mockedReadToken.mockResolvedValue('stored-token');
    mockedGetCurrentUser.mockRejectedValue(new ApiError(403, 'FORBIDDEN', 'Access denied'));

    await useAuthStore.getState().restoreSession();

    expect(mockedClearToken).not.toHaveBeenCalled();
    expect(useAuthStore.getState()).toMatchObject({
      status: 'error',
      accessToken: 'stored-token',
      user: null,
    });
  });

  it('keeps the token on network or 5xx bootstrap failures', async () => {
    mockedReadToken.mockResolvedValue('stored-token');
    mockedGetCurrentUser.mockRejectedValue(new ApiError(500, 'INTERNAL_ERROR', 'Internal server error'));

    await useAuthStore.getState().restoreSession();

    expect(mockedClearToken).not.toHaveBeenCalled();
    expect(useAuthStore.getState()).toMatchObject({
      status: 'error',
      accessToken: 'stored-token',
      bootstrapError: 'Internal server error',
    });
  });

  it('persists a successful login then resolves /auth/me', async () => {
    mockedLogin.mockResolvedValue({
      accessToken: 'new-token',
      tokenType: 'Bearer',
      expiresAt: '2026-09-15T05:00:00Z',
    });
    mockedGetCurrentUser.mockResolvedValue(candidate);

    await useAuthStore.getState().login('candidate@example.com', 'password');

    expect(mockedWriteToken).toHaveBeenCalledWith('new-token');
    expect(mockedSetApiAccessToken).toHaveBeenCalledWith('new-token');
    expect(useAuthStore.getState()).toMatchObject({
      status: 'authenticated',
      accessToken: 'new-token',
      user: candidate,
    });
  });

  it('keeps no session after invalid login credentials', async () => {
    mockedLogin.mockRejectedValue(new ApiError(401, 'INVALID_CREDENTIALS', 'Invalid credentials'));

    await expect(useAuthStore.getState().login('candidate@example.com', 'wrong')).rejects.toMatchObject({
      code: 'INVALID_CREDENTIALS',
    });

    expect(mockedWriteToken).not.toHaveBeenCalled();
    expect(useAuthStore.getState()).toMatchObject({ status: 'unauthenticated', accessToken: null, user: null });
  });

  it('logout clears SecureStore, memory token and user state', async () => {
    useAuthStore.setState({ status: 'authenticated', accessToken: 'token', user: candidate, bootstrapError: null });

    await useAuthStore.getState().logout();

    expect(mockedClearToken).toHaveBeenCalledTimes(1);
    expect(mockedSetApiAccessToken).toHaveBeenLastCalledWith(null);
    expect(useAuthStore.getState()).toMatchObject({ status: 'unauthenticated', accessToken: null, user: null });
  });
});
