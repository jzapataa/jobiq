import { create } from 'zustand';

import { createInitialAuthState, type AuthStateSnapshot } from './authState';

export const useAuthStore = create<AuthStateSnapshot>()(() => createInitialAuthState());
