import { StyleSheet, Text, type TextProps } from 'react-native';

import { useThemeColor } from '@/theme/useThemeColor';

export type ThemedTextProps = TextProps & {
  type?: 'default' | 'title' | 'subtitle';
};

export function ThemedText({ style, type = 'default', ...rest }: ThemedTextProps) {
  const color = useThemeColor(type === 'subtitle' ? 'muted' : 'text');

  return (
    <Text
      style={[
        { color },
        type === 'default' ? styles.default : undefined,
        type === 'title' ? styles.title : undefined,
        type === 'subtitle' ? styles.subtitle : undefined,
        style,
      ]}
      {...rest}
    />
  );
}

const styles = StyleSheet.create({
  default: {
    fontFamily: 'MontserratRegular',
    fontSize: 16,
    lineHeight: 24,
  },
  title: {
    fontFamily: 'PoppinsBold',
    fontSize: 32,
    lineHeight: 38,
  },
  subtitle: {
    fontFamily: 'MontserratRegular',
    fontSize: 15,
    lineHeight: 22,
  },
});
