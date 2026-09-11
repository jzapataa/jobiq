import { StyleSheet, Text, View } from 'react-native';

import { foundationTheme } from '@/theme/foundationTheme';

export default function FoundationScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Jobiq</Text>
      <Text style={styles.subtitle}>Foundation ready</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: foundationTheme.background,
    padding: 24,
  },
  title: {
    color: foundationTheme.foreground,
    fontSize: 32,
    fontWeight: '700',
  },
  subtitle: {
    color: foundationTheme.muted,
    fontSize: 16,
    marginTop: 8,
  },
});
