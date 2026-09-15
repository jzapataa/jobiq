import { Redirect, Stack } from 'expo-router';

import { useAuthStore } from '@/features/auth/store/useAuthStore';

export default function PublicLayout() {
  const status = useAuthStore((state) => state.status);

  if (status === 'authenticated') {
    return <Redirect href="/" />;
  }

  return <Stack screenOptions={{ headerShown: false }} />;
}
