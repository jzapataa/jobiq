import * as SecureStore from 'expo-secure-store';

import { appConfig, type AppEnvironment } from '../config/env';

export function getAccessTokenStorageKey(appEnv: AppEnvironment = appConfig.appEnv): string {
  return `jobiq.${appEnv}.accessToken`;
}

export async function readStoredAccessToken(): Promise<string | null> {
  return SecureStore.getItemAsync(getAccessTokenStorageKey());
}

export async function writeStoredAccessToken(token: string): Promise<void> {
  await SecureStore.setItemAsync(getAccessTokenStorageKey(), token);
}

export async function clearStoredAccessToken(): Promise<void> {
  await SecureStore.deleteItemAsync(getAccessTokenStorageKey());
}
