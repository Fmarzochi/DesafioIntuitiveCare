import axios from 'axios';

// Define o endereço do seu Backend Java
const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export default api;