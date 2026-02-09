import { apiClient } from '../lib/api';
import { Task, TaskRequest, PaginatedResponse } from '@/types/project';

export const taskApi = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Task>> => {
    const response = await apiClient.get<PaginatedResponse<Task>>('/api/tasks', params);
    return response.data;
  },

  getById: async (id: string): Promise<Task> => {
    const response = await apiClient.get<Task>(`/api/tasks/${id}`);
    return response.data;
  },

  create: async (task: TaskRequest): Promise<Task> => {
    const response = await apiClient.post<Task>('/api/tasks', task);
    return response.data;
  },

  update: async (id: string, task: TaskRequest): Promise<Task> => {
    const response = await apiClient.put<Task>(`/api/tasks/${id}`, task);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/tasks/${id}`);
  },

  changeStatus: async (id: string, status: Task['status']): Promise<void> => {
    await apiClient.patch(`/api/tasks/${id}/status`, status);
  },

  assignTask: async (id: string, assignedToId: string): Promise<void> => {
    await apiClient.patch(`/api/tasks/${id}/assign`, assignedToId);
  },

  getByProject: async (
    projectId: string,
    params?: {
      page?: number;
      size?: number;
      sort?: string;
      direction?: string;
    }
  ): Promise<PaginatedResponse<Task>> => {
    const response = await apiClient.get<PaginatedResponse<Task>>(
      `/api/tasks/project/${projectId}`,
      params
    );
    return response.data;
  },

  getOverdue: async (params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Task>> => {
    const response = await apiClient.get<PaginatedResponse<Task>>('/api/tasks/overdue', params);
    return response.data;
  },

  search: async (searchTerm: string, params?: {
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }): Promise<PaginatedResponse<Task>> => {
    const response = await apiClient.get<PaginatedResponse<Task>>('/api/tasks/search', {
      ...params,
      searchTerm,
    });
    return response.data;
  },

  getStatistics: async (): Promise<Record<string, any>> => {
    const response = await apiClient.get('/api/tasks/statistics');
    return response.data;
  },
};
