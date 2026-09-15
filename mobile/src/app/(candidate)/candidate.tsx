import { Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function CandidateSessionScreen() {
  const logout = useAuthStore((state) => state.logout);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');

  return (
    <View style={[styles.container, { backgroundColor }]}>
      <ThemedText type="title">Sesión Candidate</ThemedText>
      <ThemedText type="subtitle">Autenticación completada. El perfil llegará en Slice 4.</ThemedText>
      <Pressable onPress={() => void logout()} style={[styles.button, { backgroundColor: primaryColor }]}>
        <ThemedText>Cerrar sesión</ThemedText>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', paddingHorizontal: 40, gap: 12 },
  button: { marginTop: 12, minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
});
