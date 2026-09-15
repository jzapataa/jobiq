import { Link, Redirect } from 'expo-router';
import { Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function CandidateSessionScreen() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');

  if (user?.role === 'CANDIDATE' && !user.profileComplete) {
    return <Redirect href="/(candidate)/onboarding" />;
  }

  return (
    <View style={[styles.container, { backgroundColor }]}>
      <ThemedText type="title">Candidate</ThemedText>
      <ThemedText type="subtitle">Perfil completado. Las funciones de Jobs llegarán en un slice posterior.</ThemedText>
      <Link href="/(candidate)/profile" asChild>
        <Pressable style={[styles.button, { backgroundColor: primaryColor }]}><ThemedText>Editar perfil</ThemedText></Pressable>
      </Link>
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
