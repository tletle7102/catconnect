import api from './client';

export interface LoginRequest {
  username: string;
  password: string;
  stayLoggedIn?: boolean;
}

export interface LoginResponse {
  username: string;
  role: string;
  profileImageUrl: string | null;
  authenticated: boolean;
}

export const authApi = {
  login: (data: LoginRequest) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  check: () => api.get<{ data: LoginResponse }>('/auth/check'),
  refresh: () => api.post('/auth/refresh'),
};
