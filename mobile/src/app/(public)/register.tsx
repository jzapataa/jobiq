import { useState } from 'react';
import { router } from 'expo-router';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native';

import { ApiError } from '@/core/api/apiError';
import { ThemedText } from '@/components/ThemedText';
import { ThemeTextInput } from '@/components/ThemeTextInput';
import { registerRequest } from '@/features/auth/api/authApi';
import { useThemeColor } from '@/theme/useThemeColor';
import type { UserRole } from '@/types/auth';

export default function RegisterScreen() {
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const borderColor = useThemeColor('border');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState<UserRole>('CANDIDATE');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    setLoading(true);
    setError(null);
    try {
      await registerRequest({ email, password, name, role });
      router.replace('/(public)/login');
    } catch (registerError) {
      setError(registerError instanceof ApiError ? registerError.message : 'No se pudo crear la cuenta.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={[styles.screen, { backgroundColor }]}
    >
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <ThemedText type="title">Crear cuenta</ThemedText>
        <ThemedText type="subtitle" style={styles.subtitle}>
          Regístrate y después inicia sesión
        </ThemedText>

        <View style={styles.form}>
          <ThemeTextInput value={name} onChangeText={setName} placeholder="Nombre" editable={!loading} />
          <ThemeTextInput
            value={email}
            onChangeText={setEmail}
            placeholder="Correo electrónico"
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
            editable={!loading}
          />
          <ThemeTextInput
            value={password}
            onChangeText={setPassword}
            placeholder="Contraseña"
            secureTextEntry
            autoCapitalize="none"
            editable={!loading}
          />

          <View style={styles.roles}>
            {(['CANDIDATE', 'RECRUITER'] as const).map((candidateRole) => (
              <Pressable
                key={candidateRole}
                disabled={loading}
                onPress={() => setRole(candidateRole)}
                style={[
                  styles.roleButton,
                  { borderColor },
                  role === candidateRole && { borderColor: primaryColor },
                ]}
              >
                <ThemedText>{candidateRole === 'CANDIDATE' ? 'Candidato' : 'Recruiter'}</ThemedText>
              </Pressable>
            ))}
          </View>

          {error ? <ThemedText style={styles.error}>{error}</ThemedText> : null}
          <Pressable
            disabled={loading}
            onPress={() => void submit()}
            style={[styles.button, { backgroundColor: primaryColor }, loading && styles.disabled]}
          >
            {loading ? <ActivityIndicator /> : <ThemedText>Crear cuenta</ThemedText>}
          </Pressable>
          <Pressable disabled={loading} onPress={() => router.replace('/(public)/login')} style={styles.link}>
            <ThemedText type="subtitle">Volver a ingresar</ThemedText>
          </Pressable>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { flexGrow: 1, justifyContent: 'center', paddingHorizontal: 40, paddingVertical: 40 },
  subtitle: { marginTop: 4 },
  form: { marginTop: 24 },
  roles: { flexDirection: 'row', gap: 12, marginBottom: 16 },
  roleButton: { flex: 1, minHeight: 48, borderWidth: 1, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
  error: { marginBottom: 12 },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
  disabled: { opacity: 0.6 },
  link: { alignItems: 'center', paddingVertical: 16 },
});
