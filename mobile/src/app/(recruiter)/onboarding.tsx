import { Redirect } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet } from 'react-native';

import { ThemeTextInput } from '@/components/ThemeTextInput';
import { ThemedText } from '@/components/ThemedText';
import { toApiError } from '@/core/api/apiError';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import type { RecruiterProfilePayload } from '@/features/recruiter-profile/api/recruiterProfileApi';
import { saveRecruiterOnboarding } from '@/features/recruiter-profile/profileFlow';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RecruiterOnboardingScreen() {
  const user = useAuthStore((state) => state.user);
  const restoreSession = useAuthStore((state) => state.restoreSession);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const [position, setPosition] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [website, setWebsite] = useState('');
  const [logoUrl, setLogoUrl] = useState('');
  const [location, setLocation] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (user?.role === 'RECRUITER' && user.profileComplete) {
    return <Redirect href="/(recruiter)/recruiter" />;
  }

  async function submit() {
    setSubmitting(true);
    setError(null);
    const payload: RecruiterProfilePayload = {
      position,
      company: {
        name,
        description,
        website: nullable(website),
        logoUrl: nullable(logoUrl),
        location,
      },
    };
    try {
      await saveRecruiterOnboarding(payload, restoreSession);
    } catch (caught) {
      const apiError = toApiError(caught);
      setError(apiError.fieldErrors.length > 0
        ? apiError.fieldErrors.map((field) => `${field.field}: ${field.message}`).join('\n')
        : apiError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <ScrollView contentContainerStyle={[styles.container, { backgroundColor }]} keyboardShouldPersistTaps="handled">
      <ThemedText type="title">Perfil Recruiter</ThemedText>
      <ThemedText type="subtitle">Configura tu posición y la empresa vinculada.</ThemedText>
      <ThemeTextInput placeholder="Posición" value={position} onChangeText={setPosition} maxLength={120} />
      <ThemeTextInput placeholder="Empresa" value={name} onChangeText={setName} maxLength={160} />
      <ThemeTextInput placeholder="Descripción" value={description} onChangeText={setDescription} maxLength={1500} multiline style={styles.multiline} />
      <ThemeTextInput placeholder="Website (opcional)" value={website} onChangeText={setWebsite} maxLength={500} autoCapitalize="none" />
      <ThemeTextInput placeholder="Logo URL (opcional)" value={logoUrl} onChangeText={setLogoUrl} maxLength={500} autoCapitalize="none" />
      <ThemeTextInput placeholder="Ubicación" value={location} onChangeText={setLocation} maxLength={120} />
      {error ? <ThemedText type="subtitle">{error}</ThemedText> : null}
      <Pressable disabled={submitting} onPress={() => void submit()} style={[styles.button, { backgroundColor: primaryColor }]}>
        {submitting ? <ActivityIndicator /> : <ThemedText>Guardar onboarding</ThemedText>}
      </Pressable>
    </ScrollView>
  );
}

function nullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, paddingHorizontal: 24, paddingVertical: 36 },
  multiline: { minHeight: 120, textAlignVertical: 'top' },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
});
