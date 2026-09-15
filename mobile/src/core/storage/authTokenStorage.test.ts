import { getAccessTokenStorageKey } from './authTokenStorage';

describe('auth token storage key', () => {
  it('is scoped by environment', () => {
    expect(getAccessTokenStorageKey('local')).toBe('jobiq.local.accessToken');
    expect(getAccessTokenStorageKey('dev')).toBe('jobiq.dev.accessToken');
    expect(getAccessTokenStorageKey('prod')).toBe('jobiq.prod.accessToken');
  });
});
