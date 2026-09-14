import { useColorScheme } from 'react-native';

import { foundationTheme, type ThemeColorName } from './foundationTheme';

export function useThemeColor(colorName: ThemeColorName): string {
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  return foundationTheme[colorScheme][colorName];
}
