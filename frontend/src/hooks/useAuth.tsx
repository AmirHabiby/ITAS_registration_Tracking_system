import { createContext, useContext, useEffect, useMemo, useState } from "react";
import {
  getCurrentUser,
  login as loginRequest,
  type AuthUser,
} from "../services/authService";

type AuthContextValue = {
  user: AuthUser | null;
  token: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("rtts.token"),
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void refreshUser().finally(() => setLoading(false));
  }, []);

  async function refreshUser() {
    const storedToken = localStorage.getItem("rtts.token");
    if (!storedToken) {
      setUser(null);
      setToken(null);
      return;
    }
    setToken(storedToken);
    const currentUser = await getCurrentUser();
    setUser(currentUser);
  }

  async function login(username: string, password: string) {
    const response = await loginRequest(username, password);
    localStorage.setItem("rtts.token", response.token);
    setToken(response.token);
    await refreshUser();
  }

  function logout() {
    localStorage.removeItem("rtts.token");
    setToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({ user, token, loading, login, logout, refreshUser }),
    [user, token, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
