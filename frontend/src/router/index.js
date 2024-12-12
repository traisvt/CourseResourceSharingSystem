import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/resources'
        },
        {
          path: 'resources',
          component: () => import('../views/resources/ResourceList.vue')
        },
        {
          path: 'assignments',
          component: () => import('../views/assignments/AssignmentList.vue')
        },
        {
          path: 'submissions',
          component: () => import('../views/submissions/SubmissionList.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!userStore.token) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
