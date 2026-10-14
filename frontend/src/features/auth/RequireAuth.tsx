import { Navigate, Outlet, useLocation } from "react-router"
import { useSession } from "./session"

export function RequireAuth() {
    const { user, isLoading } = useSession()
    const location = useLocation()

    if (isLoading) {
        return <p className="p-6 text-muted-foreground">Carregando...</p>
    }

    if (!user) {
        return <Navigate to="/login" replace state={{ from: location }} />
    }

    return <Outlet />
}