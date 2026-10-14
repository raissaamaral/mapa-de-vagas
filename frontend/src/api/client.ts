export type FieldErrors = Record<string, string>

export class ApiError extends Error {
  readonly status: number
  readonly errors: FieldErrors

  constructor(status: number, message: string, errors: FieldErrors = {}) {
    super(message)
    this.name = "ApiError"
    this.status = status
    this.errors = errors
  }
}

export const GENERIC_ERROR_MESSAGE = "Algo deu errado. Tente novamente."

const BASE_PATH = "/api"
const CSRF_COOKIE = "XSRF-TOKEN"
const CSRF_HEADER = "X-XSRF-TOKEN"
const UNSAFE_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"])

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE"

interface RequestOptions {
  method?: HttpMethod
  body?: unknown
}

let unauthorizedHandler: (() => void) | null = null

// The session registers this handler; the client only reports the 401
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler
}

export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {}
): Promise<T> {
  const method = options.method ?? "GET"
  const headers = new Headers()

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json")
  }

  // Read on every call: the backend may issue a new token at any time
  if (UNSAFE_METHODS.has(method)) {
    const csrfToken = readCookie(CSRF_COOKIE)
    if (csrfToken) {
      headers.set(CSRF_HEADER, csrfToken)
    }
  }

  const response = await fetch(`${BASE_PATH}${path}`, {
    method,
    headers,
    credentials: "include",
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  if (!response.ok) {
    if (response.status === 401) {
      unauthorizedHandler?.()
    }
    throw await toApiError(response)
  }

  // Some responses have no body (e.g. 204 No Content)
  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

function readCookie(name: string): string | null {
  const prefix = `${name}=`
  for (const part of document.cookie.split("; ")) {
    if (part.startsWith(prefix)) {
      return decodeURIComponent(part.slice(prefix.length))
    }
  }
  return null
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    const data: unknown = await response.json()
    if (isApiErrorBody(data)) {
      return new ApiError(
        response.status,
        data.message,
        isFieldErrors(data.errors) ? data.errors : {}
      )
    }
  } catch {
    // The body is empty or not JSON (e.g. a 401 from the security filter)
  }
  return new ApiError(response.status, GENERIC_ERROR_MESSAGE)
}

function isApiErrorBody(
  value: unknown
): value is { message: string; errors?: unknown } {
  return (
    typeof value === "object" &&
    value !== null &&
    typeof (value as { message?: unknown }).message === "string"
  )
}

function isFieldErrors(value: unknown): value is FieldErrors {
  return (
    typeof value === "object" &&
    value !== null &&
    Object.values(value).every(
      (fieldMessage) => typeof fieldMessage === "string"
    )
  )
}
