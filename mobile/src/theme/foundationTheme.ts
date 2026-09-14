export const foundationTheme = {
  light: {
    background: '#F7F7F7',
    text: '#171717',
    muted: '#666666',
    border: '#D4D4D4',
    inputBackground: '#FFFFFF',
    primary: '#F39C12',
  },
  dark: {
    background: '#1F2B43',
    text: '#ECEDEE',
    muted: '#B8C0CC',
    border: '#516078',
    inputBackground: '#273650',
    primary: '#F39C12',
  },
} as const;

export type ThemeColorName = keyof typeof foundationTheme.light & keyof typeof foundationTheme.dark;
