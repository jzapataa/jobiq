import { Redirect } from 'expo-router';
import { ActivityIndicator, Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { resolveAuthRoute } from '@/features/auth/routing/authRoute';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RootIndex() {
  const status = useAuthStore((state) => state.status);
  const user = useAuthStore((state) => state.user);
  const bootstrapError = useAuthStore((state) => state.bootstrapError);
  const restoreSession = useAuthStore((state) => state.restoreSession);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');

  const route = resolveAuthRoute(status, user);
  if (route) {
    return <Redirect href={route} />;
  }

  if (status === 'checking') {
    return (
      <View style={[styles.centered, { backgroundColor }]}>
        <ActivityIndicator size="large" color={primaryColor} />
      </View>
    );
  }

  if (status === 'error') {
    return (
      <View style={[styles.centered, styles.errorContent, { backgroundColor }]}>
        <ThemedText type="subtitle">{bootstrapError ?? 'No se pudo restaurar la sesión.'}</ThemedText>
        <Pressable style={[styles.button, { backgroundColor: primaryColor }]} onPress={() => void restoreSession()}>
          <ThemedText>Reintentar</ThemedText>
        </Pressable>
      </View>
    );
  }

  return null;
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorContent: {
    gap: 16,
    paddingHorizontal: 40,
  },
  button: {
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
});
