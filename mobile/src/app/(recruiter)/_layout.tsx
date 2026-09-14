import { Redirect, Stack } from 'expo-router';

import { useAuthStore } from '@/features/auth/store/useAuthStore';

export default function RecruiterLayout() {
  const status = useAuthStore((state) => state.status);
  const role = useAuthStore((state) => state.user?.role);

  if (status !== 'authenticated' || role !== 'RECRUITER') {
    return <Redirect href="/" />;
  }

  return <Stack screenOptions={{ headerShown: false }} />;
}
