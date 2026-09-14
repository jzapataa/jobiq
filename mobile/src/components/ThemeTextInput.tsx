import { useState } from 'react';
import { StyleSheet, TextInput, type TextInputProps } from 'react-native';

import { useThemeColor } from '@/theme/useThemeColor';

export function ThemeTextInput({ style, onBlur, onFocus, ...rest }: TextInputProps) {
  const [focused, setFocused] = useState(false);
  const textColor = useThemeColor('text');
  const mutedColor = useThemeColor('muted');
  const borderColor = useThemeColor('border');
  const primaryColor = useThemeColor('primary');
  const inputBackground = useThemeColor('inputBackground');

  return (
    <TextInput
      {...rest}
      placeholderTextColor={mutedColor}
      onFocus={(event) => {
        setFocused(true);
        onFocus?.(event);
      }}
      onBlur={(event) => {
        setFocused(false);
        onBlur?.(event);
      }}
      style={[
        styles.input,
        {
          backgroundColor: inputBackground,
          borderColor: focused ? primaryColor : borderColor,
          color: textColor,
        },
        style,
      ]}
    />
  );
}

const styles = StyleSheet.create({
  input: {
    borderWidth: 1,
    borderRadius: 8,
    fontFamily: 'MontserratRegular',
    fontSize: 16,
    minHeight: 48,
    marginBottom: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
});
