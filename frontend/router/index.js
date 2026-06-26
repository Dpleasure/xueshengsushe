import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/auth',
    name: 'AuthChoice',
    component: () => import('@/views/AuthChoice.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '系统首页' }
      },
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/Building.vue'),
        meta: { title: '宿舍楼信息' }
      },
      {
        path: 'dormitory',
        name: 'Dormitory',
        component: () => import('@/views/Dormitory.vue'),
        meta: { title: '宿舍信息' }
      },
      {
        path: 'accommodation',
        name: 'Accommodation',
        component: () => import('@/views/Accommodation.vue'),
        meta: { title: '住宿信息' }
      },
      {
        path: 'change',
        name: 'Change',
        component: () => import('@/views/Change.vue'),
        meta: { title: '换宿信息' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/Repair.vue'),
        meta: { title: '宿舍报修' }
      },
      {
        path: 'visit',
        name: 'Visit',
        component: () => import('@/views/Visit.vue'),
        meta: { title: '来访登记' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { title: '管理员信息' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/Student.vue'),
        meta: { title: '学生信息' }
      },
      {
        path: 'apply-change',
        name: 'ApplyChange',
        component: () => import('@/views/ApplyChange.vue'),
        meta: { title: '申请换宿' }
      },
      {
        path: 'apply-repair',
        name: 'ApplyRepair',
        component: () => import('@/views/ApplyRepair.vue'),
        meta: { title: '申请报修' }
      },
      {
        path: 'apply-visit',
        name: 'ApplyVisit',
        component: () => import('@/views/ApplyVisit.vue'),
        meta: { title: '申请登记' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/auth', '/login', '/register']

  if (!token && !publicPages.includes(to.path)) {
    next('/auth')
    return
  }

  if (token && publicPages.includes(to.path)) {
    next('/home')
    return
  }

  next()
})

export default router
