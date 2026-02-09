import { apiClient } from '@/lib/api';
import { LoginRequest, AuthResponse } from '@/types/auth';

export const authApi = {
  login: async (credentials: LoginRequest, tenantId: string): Promise<AuthResponse> => {
    apiClient.setTenantId(tenantId);
    const response = await apiClient.post<AuthResponse>('/api/auth/login', credentials);
    return response.data;
  },

  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/api/auth/refresh', {
      refreshToken,
    });
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout');
    apiClient.clearAuth();
  },

  validateToken: async (): Promise<{ valid: boolean; message: string }> => {
    const response = await apiClient.get('/api/auth/validate');
    return response.data;
  },

  healthCheck: async (): Promise<{ status: string; service: string }> => {
    const response = await apiClient.get('/api/auth/health');
    return response.data;
  },
};
