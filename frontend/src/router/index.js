import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/init', name: 'init', component: () => import('../views/InitView.vue'), meta: { public: true } },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue'), meta: { public: true } },
  { path: '/', name: 'search', component: () => import('../views/SearchView.vue') },
  { path: '/flight/:id', name: 'flight-detail', component: () => import('../views/FlightDetailView.vue') },
  { path: '/tracking/:id?', name: 'tracking', component: () => import('../views/TrackingView.vue') },
  { path: '/booking/:id', name: 'booking', component: () => import('../views/BookingView.vue') },
  { path: '/payment/:id', name: 'payment', component: () => import('../views/PaymentView.vue') },
  { path: '/orders', name: 'orders', component: () => import('../views/OrderListView.vue') },
  { path: '/orders/:id', name: 'order-detail', component: () => import('../views/OrderDetailView.vue') },
  { path: '/profile', name: 'profile', component: () => import('../views/ProfileView.vue') },
  { path: '/admin/users', name: 'admin-users', component: () => import('../views/AdminUsersView.vue'), meta: { admin: true } },
  { path: '/admin/stats', name: 'admin-stats', component: () => import('../views/AdminStatsView.vue'), meta: { admin: true } },
  { path: '/admin/master', name: 'admin-master', component: () => import('../views/AdminMasterView.vue'), meta: { admin: true } },
  { path: '/admin/flights', name: 'admin-flights', component: () => import('../views/AdminFlightsView.vue'), meta: { merchant: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录访问受保护页 -> 登录页；管理员/商家页按角色拦
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return true
  }
  if (!auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.admin && !auth.isAdmin) {
    return { name: 'search' }
  }
  if (to.meta.merchant && !auth.canManageFlights) {
    return { name: 'search' }
  }
  return true
})

export default router
