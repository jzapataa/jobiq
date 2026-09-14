import { useState } from 'react';
import { router } from 'expo-router';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
  View,
} from 'react-native';

import { ApiError } from '@/core/api/apiError';
import { ThemedText } from '@/components/ThemedText';
import { ThemeTextInput } from '@/components/ThemeTextInput';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function LoginScreen() {
  const { height } = useWindowDimensions();
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const login = useAuthStore((state) => state.login);
  const status = useAuthStore((state) => state.status);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const loading = status === 'checking';

  const submit = async () => {
    setError(null);
    try {
      await login(email, password);
    } catch (loginError) {
      setError(loginError instanceof ApiError ? loginError.message : 'No se pudo iniciar sesión.');
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={[styles.screen, { backgroundColor }]}
    >
      <ScrollView
        contentContainerStyle={[styles.content, { paddingTop: Math.max(height * 0.28, 160) }]}
        keyboardShouldPersistTaps="handled"
      >
        <View>
          <ThemedText type="title">Ingresar</ThemedText>
          <ThemedText type="subtitle" style={styles.subtitle}>
            Por favor, ingrese para continuar
          </ThemedText>
        </View>

        <View style={styles.form}>
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
          {error ? <ThemedText style={styles.error}>{error}</ThemedText> : null}
          <Pressable
            disabled={loading}
            onPress={() => void submit()}
            style={[styles.button, { backgroundColor: primaryColor }, loading && styles.disabled]}
          >
            {loading ? <ActivityIndicator /> : <ThemedText>Ingresar</ThemedText>}
          </Pressable>
          <Pressable disabled={loading} onPress={() => router.push('/(public)/register')} style={styles.link}>
            <ThemedText type="subtitle">Crear cuenta</ThemedText>
          </Pressable>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { flexGrow: 1, paddingHorizontal: 40, paddingBottom: 40 },
  subtitle: { marginTop: 4 },
  form: { marginTop: 24 },
  error: { marginBottom: 12 },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
  disabled: { opacity: 0.6 },
  link: { alignItems: 'center', paddingVertical: 16 },
});
