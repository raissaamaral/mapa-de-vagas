import { Navigate, Route, Routes } from "react-router"
import { ApplicationsPage } from "@/features/applications/ApplicationsPage"
import { LoginPage } from "@/features/auth/LoginPage"
import { RequireAuth } from "@/features/auth/RequireAuth"

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<RequireAuth />}>
                <Route path="/applications" element={<ApplicationsPage />} />
            </Route>
            <Route path="/" element={<Navigate to="/applications" replace />} />
        </Routes>
    )
}