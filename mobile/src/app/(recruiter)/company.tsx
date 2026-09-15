import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet } from 'react-native';

import { ThemeTextInput } from '@/components/ThemeTextInput';
import { ThemedText } from '@/components/ThemedText';
import { toApiError } from '@/core/api/apiError';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { getRecruiterCompany, putRecruiterCompany } from '@/features/recruiter-profile/api/recruiterProfileApi';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RecruiterCompanyScreen() {
  const user = useAuthStore((state) => state.user);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [website, setWebsite] = useState('');
  const [logoUrl, setLogoUrl] = useState('');
  const [location, setLocation] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    void getRecruiterCompany()
      .then((company) => {
        setName(company.name);
        setDescription(company.description);
        setWebsite(company.website ?? '');
        setLogoUrl(company.logoUrl ?? '');
        setLocation(company.location);
      })
      .catch((error) => setMessage(toApiError(error).message))
      .finally(() => setLoading(false));
  }, []);

  if (user?.role === 'RECRUITER' && !user.profileComplete) {
    return <Redirect href="/(recruiter)/onboarding" />;
  }

  async function save() {
    setSaving(true);
    setMessage(null);
    try {
      await putRecruiterCompany({
        name,
        description,
        website: nullable(website),
        logoUrl: nullable(logoUrl),
        location,
      });
      setMessage('Empresa actualizada.');
    } catch (error) {
      const apiError = toApiError(error);
      setMessage(apiError.fieldErrors.length > 0
        ? apiError.fieldErrors.map((field) => `${field.field}: ${field.message}`).join('\n')
        : apiError.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <ActivityIndicator style={[styles.loading, { backgroundColor }]} />;

  return (
    <ScrollView contentContainerStyle={[styles.container, { backgroundColor }]} keyboardShouldPersistTaps="handled">
      <ThemedText type="title">Empresa</ThemedText>
      <ThemeTextInput value={name} onChangeText={setName} maxLength={160} />
      <ThemeTextInput value={description} onChangeText={setDescription} maxLength={1500} multiline style={styles.multiline} />
      <ThemeTextInput value={website} onChangeText={setWebsite} maxLength={500} autoCapitalize="none" placeholder="Website (opcional)" />
      <ThemeTextInput value={logoUrl} onChangeText={setLogoUrl} maxLength={500} autoCapitalize="none" placeholder="Logo URL (opcional)" />
      <ThemeTextInput value={location} onChangeText={setLocation} maxLength={120} />
      {message ? <ThemedText type="subtitle">{message}</ThemedText> : null}
      <Pressable disabled={saving} onPress={() => void save()} style={[styles.button, { backgroundColor: primaryColor }]}>
        {saving ? <ActivityIndicator /> : <ThemedText>Guardar empresa</ThemedText>}
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
