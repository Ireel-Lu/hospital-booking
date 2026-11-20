<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="24"><FirstAidKit /></el-icon>
        <span v-if="!isCollapse">医院预约系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Postcard /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

        <!-- 患者 -->
        <template v-if="userStore.hasRole('patient')">
          <el-menu-item index="/appointment">
            <el-icon><Calendar /></el-icon>
            <span>预约挂号</span>
          </el-menu-item>
          <el-menu-item index="/my-appointments">
            <el-icon><List /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          <el-menu-item index="/medical-records">
            <el-icon><Document /></el-icon>
            <span>就诊记录</span>
          </el-menu-item>
        </template>

        <!-- 医生 -->
        <template v-if="userStore.hasRole('doctor')">
          <el-menu-item index="/doctor/call-queue">
            <el-icon><Bell /></el-icon>
            <span>就诊叫号</span>
          </el-menu-item>
          <el-menu-item index="/doctor/schedule">
            <el-icon><Clock /></el-icon>
            <span>排班号源</span>
          </el-menu-item>
          <el-menu-item index="/doctor/medical-record">
            <el-icon><EditPen /></el-icon>
            <span>写病历</span>
          </el-menu-item>
        </template>

        <!-- 管理员 -->
        <template v-if="userStore.hasRole('admin')">
          <el-menu-item index="/admin/departments">
            <el-icon><OfficeBuilding /></el-icon>
            <span>科室管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/doctors">
            <el-icon><User /></el-icon>
            <span>医生管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/drugs">
            <el-icon><FirstAidKit /></el-icon>
            <span>药品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Tickets /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="20" class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tag size="small" effect="plain">{{ roleText }}</el-tag>
          <span class="user-info" @click="router.push('/profile')">
            <el-avatar :size="32" :src="userStore.avatar" class="avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ userStore.realName }}</span>
          </span>
          <el-button text type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
const roleText = computed(() => {
  const map = { patient: '患者', doctor: '医生', admin: '管理员' }
  return map[userStore.role] || '用户'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

/**
 * 退出登录：先请求后端把 token 加入 Redis 黑名单，再清理本地登录态
 */
const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow-x: hidden;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  font-size: 18px;
  font-weight: bold;
  background-color: #002140;
  white-space: nowrap;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.collapse-btn {
  cursor: pointer;
  color: #606266;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}
.avatar {
  background-color: #409eff;
  color: #fff;
}
.username {
  font-size: 14px;
  color: #333;
}
.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
