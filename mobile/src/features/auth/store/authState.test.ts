import { createInitialAuthState } from './authState';

describe('createInitialAuthState', () => {
  it('boots deterministically into the unauthenticated public experience', () => {
    expect(createInitialAuthState()).toEqual({
      status: 'unauthenticated',
      user: null,
      accessToken: null,
      bootstrapError: null,
    });
  });
});
