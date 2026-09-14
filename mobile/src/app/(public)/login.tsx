import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, useWindowDimensions, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { ThemeTextInput } from '@/components/ThemeTextInput';
import { useThemeColor } from '@/theme/useThemeColor';

export default function LoginScreen() {
  const { height } = useWindowDimensions();
  const backgroundColor = useThemeColor('background');

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
            placeholder="Correo electrónico"
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
          />
          <ThemeTextInput
            placeholder="Contraseña"
            secureTextEntry
            autoCapitalize="none"
          />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
  },
  content: {
    flexGrow: 1,
    paddingHorizontal: 40,
    paddingBottom: 40,
  },
  subtitle: {
    marginTop: 4,
  },
  form: {
    marginTop: 24,
  },
});
