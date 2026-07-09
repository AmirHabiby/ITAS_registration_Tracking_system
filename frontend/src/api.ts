export type LoginResponse = {
  token: string;
  expiresInSeconds: number;
  username: string;
  role: string;
  displayName: string;
};

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function login(
  username: string,
  password: string,
): Promise<LoginResponse> {
  const response = await fetch(`${baseUrl}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Login failed");
  }

  return response.json() as Promise<LoginResponse>;
}
