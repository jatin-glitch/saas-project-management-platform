import { apiClient } from '../lib/api';
import { Project, ProjectRequest, PaginatedResponse } from '@/types/project';

export const projectApi = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Project>> => {
    const response = await apiClient.get<PaginatedResponse<Project>>('/api/projects', params);
    return response.data;
  },

  getById: async (id: string): Promise<Project> => {
    const response = await apiClient.get<Project>(`/api/projects/${id}`);
    return response.data;
  },

  create: async (project: ProjectRequest): Promise<Project> => {
    const response = await apiClient.post<Project>('/api/projects', project);
    return response.data;
  },

  update: async (id: string, project: ProjectRequest): Promise<Project> => {
    const response = await apiClient.put<Project>(`/api/projects/${id}`, project);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/projects/${id}`);
  },

  changeStatus: async (id: string, status: Project['status']): Promise<void> => {
    await apiClient.patch(`/api/projects/${id}/status`, status);
  },

  getByStatus: async (
    status: Project['status'],
    params?: {
      page?: number;
      size?: number;
      sort?: string;
      direction?: string;
    }
  ): Promise<PaginatedResponse<Project>> => {
    const response = await apiClient.get<PaginatedResponse<Project>>(
      `/api/projects/status/${status}`,
      params
    );
    return response.data;
  },

  search: async (searchTerm: string, params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Project>> => {
    const response = await apiClient.get<PaginatedResponse<Project>>('/api/projects/search', {
      ...params,
      searchTerm,
    });
    return response.data;
  },

  getStatistics: async (): Promise<Record<string, any>> => {
    const response = await apiClient.get('/api/projects/statistics');
    return response.data;
  },
};
