import { API_URL, authHeaders, handleResponse } from './http'

const JSON_HEADERS = { 'Content-Type': 'application/json' }

export async function register({ name, email, password }) {
  return handleResponse(
    await fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: JSON_HEADERS,
      body: JSON.stringify({ name, email, password }),
    }),
  )
}

export async function login({ email, password }) {
  return handleResponse(
    await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: JSON_HEADERS,
      body: JSON.stringify({ email, password }),
    }),
  )
}

export async function getProfile(token) {
  return handleResponse(await fetch(`${API_URL}/users/me`, { headers: authHeaders(token) }))
}
