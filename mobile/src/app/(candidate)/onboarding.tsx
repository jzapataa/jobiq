import { Redirect } from 'expo-router';
import { useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, View } from 'react-native';

import { ThemeTextInput } from '@/components/ThemeTextInput';
import { ThemedText } from '@/components/ThemedText';
import { toApiError } from '@/core/api/apiError';
import type { CandidateProfilePayload } from '@/features/candidate-profile/api/candidateProfileApi';
import { saveCandidateOnboarding, serializeSkills } from '@/features/candidate-profile/profileFlow';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function CandidateOnboardingScreen() {
  const user = useAuthStore((state) => state.user);
  const restoreSession = useAuthStore((state) => state.restoreSession);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const [headline, setHeadline] = useState('');
  const [location, setLocation] = useState('');
  const [bio, setBio] = useState('');
  const [linkedinUrl, setLinkedinUrl] = useState('');
  const [githubUrl, setGithubUrl] = useState('');
  const [portfolioUrl, setPortfolioUrl] = useState('');
  const [skills, setSkills] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (user?.role === 'CANDIDATE' && user.profileComplete) {
    return <Redirect href="/(candidate)/candidate" />;
  }

  async function submit() {
    setSubmitting(true);
    setError(null);
    const payload: CandidateProfilePayload = {
      headline,
      location,
      bio,
      linkedinUrl: nullable(linkedinUrl),
      githubUrl: nullable(githubUrl),
      portfolioUrl: nullable(portfolioUrl),
      skills: serializeSkills(skills),
    };
    try {
      await saveCandidateOnboarding(payload, restoreSession);
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
      <ThemedText type="title">Completa tu perfil</ThemedText>
      <ThemedText type="subtitle">Añade la información básica que verá Jobiq.</ThemedText>
      <ThemeTextInput placeholder="Titular profesional" value={headline} onChangeText={setHeadline} maxLength={160} />
      <ThemeTextInput placeholder="Ubicación" value={location} onChangeText={setLocation} maxLength={120} />
      <ThemeTextInput placeholder="Bio" value={bio} onChangeText={setBio} maxLength={1200} multiline style={styles.multiline} />
      <ThemeTextInput placeholder="LinkedIn (opcional)" value={linkedinUrl} onChangeText={setLinkedinUrl} maxLength={500} autoCapitalize="none" />
      <ThemeTextInput placeholder="GitHub (opcional)" value={githubUrl} onChangeText={setGithubUrl} maxLength={500} autoCapitalize="none" />
      <ThemeTextInput placeholder="Portfolio (opcional)" value={portfolioUrl} onChangeText={setPortfolioUrl} maxLength={500} autoCapitalize="none" />
      <ThemeTextInput placeholder="Skills separadas por comas" value={skills} onChangeText={setSkills} />
      {error ? <ThemedText type="subtitle">{error}</ThemedText> : null}
      <Pressable disabled={submitting} onPress={() => void submit()} style={[styles.button, { backgroundColor: primaryColor }]}>
        {submitting ? <ActivityIndicator /> : <ThemedText>Guardar perfil</ThemedText>}
      </Pressable>
    </ScrollView>
  );
}

function nullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, paddingHorizontal: 24, paddingVertical: 36, gap: 4 },
  multiline: { minHeight: 120, textAlignVertical: 'top' },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
});
