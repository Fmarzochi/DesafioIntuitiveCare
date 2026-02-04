import { createRouter, createWebHistory } from 'vue-router'
// Importamos a tela que vamos criar no próximo passo
import OperadorasView from '../views/OperadorasView.vue'

const router = createRouter({
  // Isso mantém o histórico de navegação do navegador
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: OperadorasView
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      // Tela de gráficos que faremos depois
      component: () => import('../views/DashboardView.vue')
    }
  ]
})

export default router