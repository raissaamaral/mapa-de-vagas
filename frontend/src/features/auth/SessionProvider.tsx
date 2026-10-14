import { useEffect, type ReactNode } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { useNavigate } from "react-router"
import { setUnauthorizedHandler } from "@/api/client"
import { fetchCurrentUser } from "./api"
import { SESSION_QUERY_KEY, SessionContext } from "./session"
import type { User } from "./types"

export function SessionProvider({ children }: { children: ReactNode }) {
    const queryClient = useQueryClient()
    const navigate = useNavigate()

    const { data, isPending } = useQuery({
        queryKey: SESSION_QUERY_KEY,
        queryFn: fetchCurrentUser,
        retry: false,
        staleTime: Infinity,
    })

    useEffect(() => {
        setUnauthorizedHandler(() => {
            const currentUser = queryClient.getQueryData<User | null>(SESSION_QUERY_KEY)
            // A 401 without a session (app start, wrong password) is not an expiration
            if (!currentUser) {
                return
            }
            // Remove every cached response, so no personal data stays in memory
            queryClient.clear()
            queryClient.setQueryData(SESSION_QUERY_KEY, null)
            navigate("/login", { replace: true, state: { sessionExpired: true } })
        })
        return () => setUnauthorizedHandler(null)
    }, [queryClient, navigate])

    return (
        <SessionContext.Provider value={{ user: data ?? null, isLoading: isPending }}>
            {children}
        </SessionContext.Provider>
    )
}