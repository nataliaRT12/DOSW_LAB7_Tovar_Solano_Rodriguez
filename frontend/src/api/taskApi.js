import { API_URL, authHeaders, handleResponse } from './http'

const BASE_URL = `${API_URL}/tasks`
const JSON_HEADERS = { 'Content-Type': 'application/json' }

export async function getTasks(token) {
  return handleResponse(await fetch(BASE_URL, { headers: authHeaders(token) }))
}

export async function getTask(token, id) {
  return handleResponse(await fetch(`${BASE_URL}/${id}`, { headers: authHeaders(token) }))
}

export async function createTask(token, task) {
  return handleResponse(
    await fetch(BASE_URL, {
      method: 'POST',
      headers: authHeaders(token, JSON_HEADERS),
      body: JSON.stringify(task),
    }),
  )
}

export async function updateTask(token, id, task) {
  return handleResponse(
    await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: authHeaders(token, JSON_HEADERS),
      body: JSON.stringify(task),
    }),
  )
}

export async function deleteTask(token, id) {
  return handleResponse(
    await fetch(`${BASE_URL}/${id}`, { method: 'DELETE', headers: authHeaders(token) }),
  )
}
