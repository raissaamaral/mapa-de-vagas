import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  ApiError,
  GENERIC_ERROR_MESSAGE,
  apiRequest,
  setUnauthorizedHandler,
} from "./client"

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}

describe("apiRequest", () => {
  const fetchMock = vi.fn()

  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock)
    document.cookie = "XSRF-TOKEN=test-csrf-token"
  })

  afterEach(() => {
    fetchMock.mockReset()
    vi.unstubAllGlobals()
    setUnauthorizedHandler(null)
  })

  function sentHeaders(): Headers {
    const [, init] = fetchMock.mock.calls[0]
    return init.headers as Headers
  }

  it("sends the CSRF token on unsafe methods", async () => {
    fetchMock.mockResolvedValue(jsonResponse(201, { id: 1 }))

    await apiRequest("/applications", {
      method: "POST",
      body: { company: "Rai Corp" },
    })

    expect(sentHeaders().get("X-XSRF-TOKEN")).toBe("test-csrf-token")
  })

  it("does not send the CSRF token on GET", async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, []))

    await apiRequest("/applications")

    expect(sentHeaders().get("X-XSRF-TOKEN")).toBeNull()
  })

  it("builds an ApiError from the API error format", async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(400, {
        status: 400,
        message: "Validation failed",
        errors: { company: "must not be blank" },
      })
    )

    const request = apiRequest("/applications", { method: "POST", body: {} })

    await expect(request).rejects.toBeInstanceOf(ApiError)
    await expect(request).rejects.toMatchObject({
      status: 400,
      message: "Validation failed",
      errors: { company: "must not be blank" },
    })
  })

  it("uses a generic message when the error body is not JSON", async () => {
    fetchMock.mockResolvedValue(new Response("", { status: 500 }))

    await expect(apiRequest("/applications")).rejects.toMatchObject({
      status: 500,
      message: GENERIC_ERROR_MESSAGE,
    })
  })

  it("notifies the unauthorized handler on 401", async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    fetchMock.mockResolvedValue(new Response(null, { status: 401 }))

    await expect(apiRequest("/auth/me")).rejects.toBeInstanceOf(ApiError)

    expect(handler).toHaveBeenCalledOnce()
  })
})
