export type AppEnvironment = 'local' | 'dev' | 'prod';

export interface AppConfig {
  appEnv: AppEnvironment;
  apiUrl: string;
  apiTimeoutMs: number;
}

const DEFAULT_LOCAL_TIMEOUT_MS = 15_000;
const DEFAULT_REMOTE_TIMEOUT_MS = 90_000;

function parseEnvironment(value: string | undefined): AppEnvironment {
  if (value === 'dev' || value === 'prod') {
    return value;
  }

  return 'local';
}

function parseTimeout(value: string | undefined, fallback: number): number {
  if (!value) {
    return fallback;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

export function readAppConfig(
  environment: Record<string, string | undefined> = {},
): AppConfig {
  const appEnv = parseEnvironment(environment.EXPO_PUBLIC_APP_ENV);
  const fallbackTimeout = appEnv === 'local' ? DEFAULT_LOCAL_TIMEOUT_MS : DEFAULT_REMOTE_TIMEOUT_MS;

  return {
    appEnv,
    apiUrl: environment.EXPO_PUBLIC_API_URL ?? '',
    apiTimeoutMs: parseTimeout(environment.EXPO_PUBLIC_API_TIMEOUT_MS, fallbackTimeout),
  };
}

export const appConfig = readAppConfig({
  EXPO_PUBLIC_APP_ENV: process.env.EXPO_PUBLIC_APP_ENV,
  EXPO_PUBLIC_API_URL: process.env.EXPO_PUBLIC_API_URL,
  EXPO_PUBLIC_API_TIMEOUT_MS: process.env.EXPO_PUBLIC_API_TIMEOUT_MS,
});
