import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, View } from 'react-native';

import { ThemeTextInput } from '@/components/ThemeTextInput';
import { ThemedText } from '@/components/ThemedText';
import { toApiError } from '@/core/api/apiError';
import { useAuthStore } from '@/features/auth/store/useAuthStore';
import { getRecruiterProfile, putRecruiterProfile } from '@/features/recruiter-profile/api/recruiterProfileApi';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RecruiterProfileScreen() {
  const user = useAuthStore((state) => state.user);
  const backgroundColor = useThemeColor('background');
  const primaryColor = useThemeColor('primary');
  const [position, setPosition] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    void getRecruiterProfile()
      .then((profile) => setPosition(profile.position))
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
      await putRecruiterProfile({ position });
      setMessage('Perfil actualizado.');
    } catch (error) {
      setMessage(toApiError(error).message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <ActivityIndicator style={[styles.loading, { backgroundColor }]} />;

  return (
    <View style={[styles.container, { backgroundColor }]}>
      <ThemedText type="title">Perfil Recruiter</ThemedText>
      <ThemeTextInput value={position} onChangeText={setPosition} maxLength={120} placeholder="Posición" />
      {message ? <ThemedText type="subtitle">{message}</ThemedText> : null}
      <Pressable disabled={saving} onPress={() => void save()} style={[styles.button, { backgroundColor: primaryColor }]}>
        {saving ? <ActivityIndicator /> : <ThemedText>Guardar posición</ThemedText>}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  loading: { flex: 1 },
  container: { flex: 1, paddingHorizontal: 24, paddingVertical: 36 },
  button: { minHeight: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
});
