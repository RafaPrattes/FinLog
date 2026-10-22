import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

let onUnauthorized = null

export function setOnUnauthorized(callback) {
  onUnauthorized = callback
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    const url = error.config?.url || ''
    const isAuth = url.includes('/auth/login') || url.includes('/auth/logout')
    if (error.response?.status === 401 && !isAuth && onUnauthorized) {
      onUnauthorized()
    }
    return Promise.reject(error)
  },
)

export default api
