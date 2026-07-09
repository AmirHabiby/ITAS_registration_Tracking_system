import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function RoleHomeRedirect() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to={`/${user.role.toLowerCase()}/dashboard`} replace />;
}
