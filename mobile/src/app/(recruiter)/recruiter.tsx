import { Redirect, router } from 'expo-router';
import { Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RecruiterSessionScreen() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');

  if (user?.role === 'RECRUITER' && !user.profileComplete) {
    return <Redirect href="/(recruiter)/onboarding" />;
  }

  return (
    <View style={[styles.container, { backgroundColor }]}>
      <ThemedText type="title">Recruiter</ThemedText>
      <ThemedText type="subtitle">Perfil y empresa completados. Jobs llegará en un slice posterior.</ThemedText>
      <Pressable
        onPress={() => router.push('/(recruiter)/profile')}
        style={[styles.button, { backgroundColor: primaryColor }]}
      >
        <ThemedText>Editar posición</ThemedText>
      </Pressable>
      <Pressable
        onPress={() => router.push('/(recruiter)/company')}
        style={[styles.button, { backgroundColor: primaryColor }]}
      >
        <ThemedText>Editar empresa</ThemedText>
      </Pressable>
      <Pressable onPress={() => void logout()} style={[styles.button, { backgroundColor: primaryColor }]}>
        <ThemedText>Cerrar sesión</ThemedText>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', paddingHorizontal: 40, gap: 12 },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
});
