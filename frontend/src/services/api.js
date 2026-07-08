import axios from 'axios';

const API_BASE_URL = '/api/v1/atm';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || 'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

export const atmAPI = {
  login: (accountNumber, pin) =>
    api.post('/login', { accountNumber, pin }),

  withdraw: (accountNumber, pin, amount) =>
    api.post('/withdraw', { accountNumber, pin, amount }),

  deposit: (accountNumber, amount) =>
    api.post('/deposit', { accountNumber, amount }),

  transfer: (fromAccount, toAccount, pin, amount, description) =>
    api.post('/transfer', { fromAccount, toAccount, pin, amount, description }),

  getBalance: (accountNumber) =>
    api.get('/balance', { params: { accountNumber } }),

  getStatement: (accountNumber) =>
    api.get('/statement', { params: { accountNumber } }),

  changePin: (accountNumber, oldPin, newPin) =>
    api.put('/change-pin', { accountNumber, oldPin, newPin }),

  logout: (accountNumber) =>
    api.post('/logout', null, { params: { accountNumber } }),

  getATMStatus: () =>
    api.get('/status'),
};

export default api;