import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import OperadorasView from '../views/OperadorasView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/operadoras', // CORREÇÃO: Mudado de '/pesquisa' para '/operadoras' para coincidir com o Menu
      name: 'operadoras',
      component: OperadorasView
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue')
    },
    {
      path: '/operadoras/:registroAns',
      name: 'detalhes',
      component: () => import('../views/OperadoraDetalhes.vue'),
      props: true
    }
  ]
})

export default router