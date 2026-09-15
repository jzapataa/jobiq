import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet } from 'react-native';

import { ThemeTextInput } from '@/components/ThemeTextInput';
import { ThemedText } from '@/components/ThemedText';
import { toApiError } from '@/core/api/apiError';
import { getCandidateProfile, putCandidateProfile } from '@/features/candidate-profile/api/candidateProfileApi';
import { serializeSkills } from '@/features/candidate-profile/profileFlow';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { useThemeColor } from '@/theme/useThemeColor';

export default function CandidateProfileScreen() {
  const user = useAuthStore((state) => state.user);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const [headline, setHeadline] = useState('');
  const [location, setLocation] = useState('');
  const [bio, setBio] = useState('');
  const [linkedinUrl, setLinkedinUrl] = useState('');
  const [githubUrl, setGithubUrl] = useState('');
  const [portfolioUrl, setPortfolioUrl] = useState('');
  const [skills, setSkills] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    void getCandidateProfile()
      .then((profile) => {
        setHeadline(profile.headline);
        setLocation(profile.location);
        setBio(profile.bio);
        setLinkedinUrl(profile.linkedinUrl ?? '');
        setGithubUrl(profile.githubUrl ?? '');
        setPortfolioUrl(profile.portfolioUrl ?? '');
        setSkills(profile.skills.join(', '));
      })
      .catch((error) => setMessage(toApiError(error).message))
      .finally(() => setLoading(false));
  }, []);

  if (user?.role === 'CANDIDATE' && !user.profileComplete) {
    return <Redirect href="/(candidate)/onboarding" />;
  }

  async function save() {
    setSaving(true);
    setMessage(null);
    try {
      await putCandidateProfile({
        headline,
        location,
        bio,
        linkedinUrl: nullable(linkedinUrl),
        githubUrl: nullable(githubUrl),
        portfolioUrl: nullable(portfolioUrl),
        skills: serializeSkills(skills),
      });
      setMessage('Perfil actualizado.');
    } catch (error) {
      const apiError = toApiError(error);
      setMessage(apiError.fieldErrors.length > 0
        ? apiError.fieldErrors.map((field) => `${field.field}: ${field.message}`).join('\n')
        : apiError.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <ActivityIndicator style={[styles.loading, { backgroundColor }]} />;
  }

  return (
    <ScrollView contentContainerStyle={[styles.container, { backgroundColor }]} keyboardShouldPersistTaps="handled">
      <ThemedText type="title">Tu perfil</ThemedText>
      <ThemeTextInput value={headline} onChangeText={setHeadline} maxLength={160} />
      <ThemeTextInput value={location} onChangeText={setLocation} maxLength={120} />
      <ThemeTextInput value={bio} onChangeText={setBio} maxLength={1200} multiline style={styles.multiline} />
      <ThemeTextInput value={linkedinUrl} onChangeText={setLinkedinUrl} maxLength={500} autoCapitalize="none" placeholder="LinkedIn (opcional)" />
      <ThemeTextInput value={githubUrl} onChangeText={setGithubUrl} maxLength={500} autoCapitalize="none" placeholder="GitHub (opcional)" />
      <ThemeTextInput value={portfolioUrl} onChangeText={setPortfolioUrl} maxLength={500} autoCapitalize="none" placeholder="Portfolio (opcional)" />
      <ThemeTextInput value={skills} onChangeText={setSkills} placeholder="Skills separadas por comas" />
      {message ? <ThemedText type="subtitle">{message}</ThemedText> : null}
      <Pressable disabled={saving} onPress={() => void save()} style={[styles.button, { backgroundColor: primaryColor }]}>
        {saving ? <ActivityIndicator /> : <ThemedText>Guardar cambios</ThemedText>}
      </Pressable>
    </ScrollView>
  );
}

function nullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

const styles = StyleSheet.create({
  loading: { flex: 1 },
  container: { flexGrow: 1, paddingHorizontal: 24, paddingVertical: 36 },
  multiline: { minHeight: 120, textAlignVertical: 'top' },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
});
