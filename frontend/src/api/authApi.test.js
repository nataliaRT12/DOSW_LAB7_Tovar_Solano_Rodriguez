import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getProfile, login, register } from './authApi'

function jsonResponse(status, body) {
  return { ok: status < 400, status, json: () => Promise.resolve(body) }
}

describe('authApi', () => {
  beforeEach(() => {
    globalThis.fetch = vi.fn()
  })

  it('posts the credentials to the login endpoint', async () => {
    fetch.mockResolvedValue(jsonResponse(200, { tokenType: 'Bearer', accessToken: 'jwt', expiresIn: 1800 }))

    const result = await login({ email: 'ana@example.com', password: 'ContrasenaDemo123!' })

    const [url, options] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/v1\/auth\/login$/)
    expect(options.method).toBe('POST')
    expect(JSON.parse(options.body)).toEqual({ email: 'ana@example.com', password: 'ContrasenaDemo123!' })
    expect(result.accessToken).toBe('jwt')
  })

  it('posts name, email and password to the register endpoint', async () => {
    fetch.mockResolvedValue(jsonResponse(201, { id: 1, role: 'USER' }))

    await register({ name: 'Ana', email: 'ana@example.com', password: 'ContrasenaDemo123!' })

    const [url, options] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/v1\/auth\/register$/)
    expect(JSON.parse(options.body)).toEqual({
      name: 'Ana',
      email: 'ana@example.com',
      password: 'ContrasenaDemo123!',
    })
  })

  it('requests the profile with the bearer token', async () => {
    fetch.mockResolvedValue(jsonResponse(200, { id: 1, name: 'Ana' }))

    await getProfile('jwt')

    const [url, options] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/v1\/users\/me$/)
    expect(options.headers.Authorization).toBe('Bearer jwt')
  })

  it('rejects with status 409 when the email already exists', async () => {
    fetch.mockResolvedValue(jsonResponse(409, { status: 409, message: 'An account with email already exists' }))

    await expect(register({ name: 'Ana', email: 'ana@example.com', password: 'x' })).rejects.toMatchObject({
      status: 409,
    })
  })
})
