import { shouldInvalidateSession } from './unauthorizedHandler';

describe('unauthorized handler', () => {
  it('invalidates only authenticated 401 responses', () => {
    expect(shouldInvalidateSession(401, true)).toBe(true);
    expect(shouldInvalidateSession(401, false)).toBe(false);
    expect(shouldInvalidateSession(403, true)).toBe(false);
    expect(shouldInvalidateSession(500, true)).toBe(false);
    expect(shouldInvalidateSession(null, true)).toBe(false);
  });
});
