<template>
  <div class="login-page">
    <div class="login-panel">
      <!-- 左侧品牌区 -->
      <div class="brand">
        <div class="brand-logo">
          <el-icon :size="40"><FirstAidKit /></el-icon>
        </div>
        <h1>医院预约挂号系统</h1>
        <p>在线预约 · 智能候诊 · 电子病历</p>
        <ul class="brand-points">
          <li><el-icon><CircleCheck /></el-icon> 按科室、医生、时段精准预约</li>
          <li><el-icon><CircleCheck /></el-icon> 支付后自动生成候诊号</li>
          <li><el-icon><CircleCheck /></el-icon> 医生端叫号与病历一体化</li>
        </ul>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-area">
        <h2>登录</h2>
        <p class="sub-title">欢迎回来，请输入您的账号信息</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="pwd">
            <el-input
              v-model="form.pwd"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <div class="footer">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  pwd: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  pwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({ username: form.username, pwd: form.pwd })
      ElMessage.success('登录成功')
      router.replace(route.query.redirect || '/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #42baa7 0%, #66c1bb 45%, #93d2da 100%);
  padding: 24px;
}
.login-panel {
  width: 900px;
  max-width: 100%;
  min-height: 520px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(6, 40, 80, 0.35);
}
.brand {
  padding: 56px 44px;
  color: #fff;
  background: linear-gradient(160deg, #42baa7 0%, #66c1bb 60%, #93d2da 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand-logo {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}
.brand h1 {
  font-size: 26px;
  margin: 0 0 12px;
  letter-spacing: 1px;
}
.brand p {
  margin: 0;
  opacity: 0.85;
}
.brand-points {
  list-style: none;
  padding: 0;
  margin: 36px 0 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-size: 13px;
  opacity: 0.92;
}
.brand-points li {
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-area {
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.form-area h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2d3d;
}
.sub-title {
  margin: 8px 0 28px;
  color: #909399;
  font-size: 13px;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
  letter-spacing: 4px;
}
.footer {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
@media (max-width: 720px) {
  .login-panel {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .brand {
    display: none;
  }
  .form-area {
    padding: 40px 28px;
  }
}
</style>
