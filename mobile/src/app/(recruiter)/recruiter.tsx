import { Link, Redirect } from 'expo-router';
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
      <Link href="/(recruiter)/profile" asChild>
        <Pressable style={[styles.button, { backgroundColor: primaryColor }]}><ThemedText>Editar posición</ThemedText></Pressable>
      </Link>
      <Link href="/(recruiter)/company" asChild>
        <Pressable style={[styles.button, { backgroundColor: primaryColor }]}><ThemedText>Editar empresa</ThemedText></Pressable>
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
