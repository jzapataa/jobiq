export type UserRole = 'CANDIDATE' | 'RECRUITER';

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  profileComplete: boolean;
}
