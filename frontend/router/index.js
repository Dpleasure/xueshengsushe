import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Layout from '@/layout/index.vue'

const routes = [
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
        meta: { title: '换寝信息' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/Repair.vue'),
        meta: { title: '寝室报修' }
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
        meta: { title: '申请换寝' }
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 白名单：不需要登录就可以访问的页面
  const whiteList = ['/login', '/register']
  
  if (!whiteList.includes(to.path) && !token) {
    // 未登录且不在白名单中，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录访问登录/注册页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router

