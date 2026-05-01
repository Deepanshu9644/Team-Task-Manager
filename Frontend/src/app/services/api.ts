const BASE_URL = "http://localhost:8080/api";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

export const getHeaders = (): HeadersInit => {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export const api = {
  async request<T>(
    endpoint: string,
    options?: RequestInit
  ): Promise<ApiResponse<T>> {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers: getHeaders(),
    });
    return response.json();
  },

  async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: "GET" });
  },

  async post<T>(endpoint: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  async put<T>(endpoint: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  async patch<T>(endpoint: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: "PATCH",
      body: JSON.stringify(data),
    });
  },

  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: "DELETE" });
  },
};

export type Role = "ADMIN" | "MEMBER";
export type TaskStatus = "TODO" | "IN_PROGRESS" | "IN_REVIEW" | "DONE";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH";
export type ProjectStatus = "ACTIVE" | "ON_HOLD" | "COMPLETED";

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  color: string;
  dueDate: string;
  status: ProjectStatus;
  createdAt: string;
  adminId: number;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  projectId: number;
  assigneeId: number;
  assigneeName?: string;
  createdAt: string;
}

export interface ProjectMember {
  userId: number;
  userName: string;
  userEmail: string;
  role: Role;
}

export interface ProjectDetail extends Project {
  tasks: Task[];
  members: ProjectMember[];
}

export interface DashboardStats {
  totalProjects: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  recentProjects: Project[];
  myTasks: Task[];
  overduedTasks: Task[];
}
