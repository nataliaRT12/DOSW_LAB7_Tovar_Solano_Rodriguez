const CATEGORY_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1') + '/categories'

async function handleResponse(response) {
  if (!response.ok) {
    let message = 'Request failed with status ' + response.status
    try {
      const body = await response.json()
      if (body.message) message = body.message
    } catch (e) {}
    throw new Error(message)
  }
  return response.status === 204 ? null : response.json()
}

export async function getCategories() {
  return handleResponse(await fetch(CATEGORY_URL))
}

export async function createCategory(category) {
  return handleResponse(
    await fetch(CATEGORY_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(category),
    }),
  )
}

export async function updateCategory(id, category) {
  return handleResponse(
    await fetch(CATEGORY_URL + '/' + id, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(category),
    }),
  )
}
