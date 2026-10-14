import config from '../config/api';

const TOKEN_KEY = 'voyaai_token';

async function authRequest(path, body, token) {
  let response;
  try {
    response = await fetch(`${config.apiBaseUrl}/auth/${path}`, {
      method: path === 'me' ? 'GET' : 'POST',
      headers: {
        ...(path === 'me' ? {} : { 'Content-Type': 'application/json' }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      ...(body ? { body: JSON.stringify(body) } : {}),
    });
  } catch {
    throw new Error('Unable to reach VoyaAI. Check that the backend is running and try again.');
  }
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = data.message || (response.status === 401
      ? 'Invalid email or password.'
      : 'Something went wrong. Please check your details and try again.');
    throw new Error(message);
  }
  return data;
}

export const authService = {
  async login(email, password) {
    if (config.useMockData) {
      const mockResult = {
        token: 'mock-jwt-token-voyaai-dev',
        user: { id: 'user-1', name: email.split('@')[0] || 'Aarav Sharma', email: email.trim() },
      };
      localStorage.setItem(TOKEN_KEY, mockResult.token);
      return mockResult;
    }
    const result = await authRequest('login', { email: email.trim(), password });
    localStorage.setItem(TOKEN_KEY, result.token);
    return result;
  },
  async signup(name, email, password) {
    if (config.useMockData) {
      const mockResult = {
        token: 'mock-jwt-token-voyaai-dev',
        user: { id: 'user-1', name: name.trim() || 'Aarav Sharma', email: email.trim() },
      };
      localStorage.setItem(TOKEN_KEY, mockResult.token);
      return mockResult;
    }
    await authRequest('register', { name: name.trim(), email: email.trim(), password });
    return this.login(email, password);
  },
  async restoreSession() {
    const token = this.getToken();
    if (!token) return null;
    if (config.useMockData) {
      return { id: 'user-1', name: 'Aarav Sharma', email: 'aarav.sharma@email.com' };
    }
    try {
      return await authRequest('me', null, token);
    } catch {
      this.logout();
      return null;
    }
  },
  logout() {
    localStorage.removeItem(TOKEN_KEY);
  },
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },
};
