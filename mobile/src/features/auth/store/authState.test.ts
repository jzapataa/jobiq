import { createInitialAuthState } from './authState';

describe('auth state foundation', () => {
  it('starts in checking state for session bootstrap', () => {
    expect(createInitialAuthState()).toEqual({
      status: 'checking',
      user: null,
      accessToken: null,
      bootstrapError: null,
    });
  });
});
