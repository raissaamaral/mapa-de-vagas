import { useLocation } from "react-router"

interface LoginLocationState {
    sessionExpired?: boolean
}

export function LoginPage() {
    const location = useLocation()
    const state = location.state as LoginLocationState | null

    return (
        <main className="mx-auto max-w-sm p-6">
            <h1 className="text-2xl font-semibold">Entrar</h1>
            {state?.sessionExpired && (
                <p role="status" className="mt-4 rounded-md border p-3 text-sm">
                    Sua sessão expirou. Entre novamente.
                </p>
            )}
            <p className="mt-4 text-muted-foreground">A tela de login chega na próxima Issue.</p>
        </main>
    )
}