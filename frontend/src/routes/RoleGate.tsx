import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function RoleGate({
  children,
  roles,
}: {
  children: React.ReactNode;
  roles: string[];
}) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!roles.includes(user.role)) {
    return <Navigate to={`/${user.role.toLowerCase()}/dashboard`} replace />;
  }

  return <>{children}</>;
}
