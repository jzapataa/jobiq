import { StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/ThemedText';
import { useThemeColor } from '@/theme/useThemeColor';

export default function RegisterScreen() {
  const backgroundColor = useThemeColor('background');

  return (
    <View style={[styles.container, { backgroundColor }]}>
      <ThemedText type="title">Crear cuenta</ThemedText>
      <ThemedText type="subtitle" style={styles.message}>
        Registro todavía no disponible.
      </ThemedText>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 40,
  },
  message: {
    marginTop: 8,
  },
});
