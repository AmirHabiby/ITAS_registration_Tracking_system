import { apiClient } from "./apiClient";

export type AuthUser = {
  id: string;
  username: string;
  role: string;
  displayName: string;
  enabled: boolean;
};

export type LoginResponse = {
  token: string;
  expiresInSeconds: number;
  username: string;
  role: string;
  displayName: string;
};

export async function login(username: string, password: string) {
  const response = await apiClient.post<LoginResponse>("/api/auth/login", {
    username,
    password,
  });
  return response.data;
}

export async function getCurrentUser() {
  const response = await apiClient.get<AuthUser>("/api/auth/me");
  return response.data;
}
