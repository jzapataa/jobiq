import { Redirect } from 'expo-router';

import { useAuthStore } from '@/features/auth/store/useAuthStore';

export default function RootIndex() {
  const status = useAuthStore((state) => state.status);

  if (status === 'unauthenticated') {
    return <Redirect href="/(public)/login" />;
  }

  return null;
}
