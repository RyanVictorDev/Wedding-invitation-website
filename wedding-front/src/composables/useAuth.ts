import { computed, ref } from 'vue'
import { api } from 'src/boot/axios'

const TOKEN_KEY = 'wedding_admin_token'
const USERNAME_KEY = 'wedding_admin_username'

const token = ref<string | null>(sessionStorage.getItem(TOKEN_KEY))
const username = ref<string | null>(sessionStorage.getItem(USERNAME_KEY))

export function useAuth () {
  const isAuthenticated = computed(() => !!token.value)

  async function login (user: string, password: string) {
    const { data } = await api.post<{ token: string, username: string }>('/auth/login', {
      username: user,
      password
    })

    token.value = data.token
    username.value = data.username
    sessionStorage.setItem(TOKEN_KEY, data.token)
    sessionStorage.setItem(USERNAME_KEY, data.username)
  }

  function logout () {
    token.value = null
    username.value = null
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(USERNAME_KEY)
  }

  function getToken () {
    return token.value
  }

  return {
    token,
    username,
    isAuthenticated,
    login,
    logout,
    getToken
  }
}

export function clearAuthSession () {
  token.value = null
  username.value = null
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USERNAME_KEY)
}
