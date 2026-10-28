import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ApiError } from './http'
import { createTask, deleteTask, getTasks } from './taskApi'

function jsonResponse(status, body) {
  return { ok: status < 400, status, json: () => Promise.resolve(body) }
}

describe('taskApi', () => {
  beforeEach(() => {
    globalThis.fetch = vi.fn()
  })

  it('sends the bearer token when listing tasks', async () => {
    fetch.mockResolvedValue(jsonResponse(200, []))

    await getTasks('ana-token')

    const [url, options] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/v1\/tasks$/)
    expect(options.headers.Authorization).toBe('Bearer ana-token')
  })

  it('sends the bearer token and json body when creating a task', async () => {
    fetch.mockResolvedValue(jsonResponse(201, { id: 1 }))

    await createTask('ana-token', { title: 'Entregar laboratorio' })

    const [, options] = fetch.mock.calls[0]
    expect(options.method).toBe('POST')
    expect(options.headers).toEqual({
      'Content-Type': 'application/json',
      Authorization: 'Bearer ana-token',
    })
    expect(JSON.parse(options.body)).toEqual({ title: 'Entregar laboratorio' })
  })

  it('returns null when the task is deleted', async () => {
    fetch.mockResolvedValue({ ok: true, status: 204 })

    await expect(deleteTask('ana-token', 3)).resolves.toBeNull()
  })

  it('throws an ApiError with the status when the token is rejected', async () => {
    fetch.mockResolvedValue(jsonResponse(401, { status: 401, message: 'Authentication required' }))

    const error = await getTasks('expired-token').catch((err) => err)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(401)
    expect(error.message).toBe('Authentication required')
  })
})
