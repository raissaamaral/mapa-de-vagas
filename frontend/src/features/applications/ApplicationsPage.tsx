import { useSession } from "@/features/auth/session"

export function ApplicationsPage() {
    const { user } = useSession()

    return (
        <main className="mx-auto max-w-5xl p-6">
            <h1 className="text-2xl font-semibold">Candidaturas</h1>
            <p className="mt-2 text-muted-foreground">Sessão ativa: {user?.email}</p>
        </main>
    )
}