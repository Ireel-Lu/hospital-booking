import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ========== 无需登录 ==========
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },

    // ========== 主布局（需要登录） ==========
    {
      path: '/',
      component: () => import('@/layouts/index.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: '个人中心' }
        },

        // 患者
        {
          path: 'appointment',
          name: 'Appointment',
          component: () => import('@/views/patient/Appointment.vue'),
          meta: { title: '预约挂号', role: 'patient' }
        },
        {
          path: 'my-appointments',
          name: 'MyAppointments',
          component: () => import('@/views/patient/MyAppointments.vue'),
          meta: { title: '我的预约', role: 'patient' }
        },
        {
          path: 'medical-records',
          name: 'MedicalRecords',
          component: () => import('@/views/patient/MedicalRecords.vue'),
          meta: { title: '就诊记录', role: 'patient' }
        },

        // 医生
        {
          path: 'doctor/call-queue',
          name: 'DoctorCallQueue',
          component: () => import('@/views/doctor/CallQueue.vue'),
          meta: { title: '就诊叫号', role: 'doctor' }
        },
        {
          path: 'doctor/schedule',
          name: 'DoctorSchedule',
          component: () => import('@/views/doctor/Schedule.vue'),
          meta: { title: '排班号源', role: 'doctor' }
        },
        {
          path: 'doctor/medical-record',
          name: 'DoctorMedicalRecord',
          component: () => import('@/views/doctor/MedicalRecord.vue'),
          meta: { title: '写病历', role: 'doctor' }
        },

        // 管理员
        {
          path: 'admin/departments',
          name: 'AdminDepartments',
          component: () => import('@/views/admin/Departments.vue'),
          meta: { title: '科室管理', role: 'admin' }
        },
        {
          path: 'admin/doctors',
          name: 'AdminDoctors',
          component: () => import('@/views/admin/Doctors.vue'),
          meta: { title: '医生管理', role: 'admin' }
        },
        {
          path: 'admin/users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/Users.vue'),
          meta: { title: '用户管理', role: 'admin' }
        },
        {
          path: 'admin/drugs',
          name: 'AdminDrugs',
          component: () => import('@/views/admin/Drugs.vue'),
          meta: { title: '药品管理', role: 'admin' }
        },
        {
          path: 'admin/logs',
          name: 'AdminLogs',
          component: () => import('@/views/admin/Logs.vue'),
          meta: { title: '操作日志', role: 'admin' }
        }
      ]
    },

    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

/**
 * 全局前置守卫：登录校验 + 角色校验
 */
router.beforeEach((to) => {
  const userStore = useUserStore()
  const hasToken = Boolean(userStore.token || localStorage.getItem('token'))

  // 登录/注册页：已登录直接进工作台
  if (to.meta.requiresAuth === false) {
    return hasToken ? '/dashboard' : true
  }

  // 未登录：记录原地址，登录后跳回
  if (!hasToken) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 角色不匹配：管理员可访问全部，其他角色只能访问自己的页面
  if (to.meta.role && !userStore.hasRole(to.meta.role, 'admin')) {
    return '/dashboard'
  }

  return true
})

export default router
