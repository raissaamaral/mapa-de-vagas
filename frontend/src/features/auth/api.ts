import { ApiError, apiRequest } from "@/api/client"
import type { User } from "./types"

// No session is a normal state, not an error: returns null on 401
export async function fetchCurrentUser(): Promise<User | null> {
    try {
        return await apiRequest<User>("/auth/me")
    } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
            return null
        }
        throw error
    }
}