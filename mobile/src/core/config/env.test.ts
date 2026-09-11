import { readAppConfig } from './env';

describe('readAppConfig', () => {
  it('uses local defaults without environment variables', () => {
    expect(readAppConfig({})).toEqual({
      appEnv: 'local',
      apiUrl: '',
      apiTimeoutMs: 15_000,
    });
  });

  it('uses the remote timeout baseline for DEV', () => {
    expect(
      readAppConfig({
        EXPO_PUBLIC_APP_ENV: 'dev',
        EXPO_PUBLIC_API_URL: 'https://jobiq-api-dev.onrender.com',
      }),
    ).toEqual({
      appEnv: 'dev',
      apiUrl: 'https://jobiq-api-dev.onrender.com',
      apiTimeoutMs: 90_000,
    });
  });
});
