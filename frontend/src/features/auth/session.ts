import { createContext, useContext } from "react"
import type { User } from "./types"

export const SESSION_QUERY_KEY = ["session"] as const

export interface SessionContextValue {
    user: User | null
    isLoading: boolean
}

export const SessionContext = createContext<SessionContextValue | null>(null)

export function useSession(): SessionContextValue {
    const context = useContext(SessionContext)
    if (!context) {
        throw new Error("useSession must be used inside SessionProvider")
    }
    return context
}