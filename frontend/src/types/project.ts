export interface Project {
  id: string;
  name: string;
  description: string;
  code: string;
  status: 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  startDate?: string;
  endDate?: string;
  estimatedHours?: number;
  budget?: number;
  currency?: string;
  isPublic: boolean;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  tenantId: number;
}

export interface ProjectRequest {
  name: string;
  description: string;
  code: string;
  status: Project['status'];
  priority: Project['priority'];
  startDate?: string;
  endDate?: string;
  estimatedHours?: number;
  budget?: number;
  currency?: string;
  isPublic: boolean;
  tags: string[];
}

export interface Task {
  id: string;
  title: string;
  description: string;
  taskNumber: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  type: 'FEATURE' | 'BUG' | 'IMPROVEMENT' | 'DOCUMENTATION';
  projectId: string;
  assignedToId?: string;
  dueDate?: string;
  startDate?: string;
  estimatedHours?: number;
  storyPoints?: number;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  tenantId: number;
}

export interface TaskRequest {
  title: string;
  description: string;
  taskNumber: string;
  status: Task['status'];
  priority: Task['priority'];
  type: Task['type'];
  projectId: string;
  assignedToId?: string;
  dueDate?: string;
  startDate?: string;
  estimatedHours?: number;
  storyPoints?: number;
  tags: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
