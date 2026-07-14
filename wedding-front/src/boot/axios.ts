import { defineBoot } from '#q-app/wrappers';
import axios, { type AxiosInstance } from 'axios';
import { clearAuthSession } from 'src/composables/useAuth';

declare module 'vue' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
    $api: AxiosInstance;
  }
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('wedding_admin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (error && typeof error === 'object' && 'response' in error) {
      const axiosError = error as { response?: { status?: number } }
      if (axiosError.response?.status === 401 && window.location.hash.includes('/dashboard')) {
        clearAuthSession();
      }
    }
    const rejection = error instanceof Error ? error : new Error('Request failed')
    return Promise.reject(rejection);
  }
);

void api.get('/health').catch((err) => {
  console.warn('API health check failed', err);
});

export default defineBoot(({ app }) => {
  app.config.globalProperties.$axios = axios;
  app.config.globalProperties.$api = api;
});

export { api };
