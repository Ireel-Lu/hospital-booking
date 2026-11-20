<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <div class="brand-logo">
          <el-icon :size="28"><FirstAidKit /></el-icon>
        </div>
        <div>
          <h2>患者注册</h2>
          <p class="sub-title">医生账号由管理员统一创建，注册成功后请返回登录</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-divider content-position="left">账号信息</el-divider>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="登录账号" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="密码" prop="pwd">
              <el-input v-model="form.pwd" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="form.confirmPwd" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="11 位手机号" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">患者资料（选填）</el-divider>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="(d) => d.getTime() > Date.now()"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="18 位身份证号" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" placeholder="姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系人电话" prop="emergencyContactPhone">
              <el-input v-model="form.emergencyContactPhone" placeholder="联系电话" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="联系地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入联系地址" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="actions">
          <el-button @click="router.push('/login')">返回登录</el-button>
          <el-button type="primary" :loading="loading" @click="handleRegister">注 册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/apis/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  pwd: '',
  confirmPwd: '',
  realName: '',
  phone: '',
  gender: '男',
  birthDate: '',
  idCard: '',
  address: '',
  emergencyContact: '',
  emergencyContactPhone: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.pwd) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (value && !/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  pwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPwd: [{ validator: validateConfirm, trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [{ validator: validateIdCard, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const { confirmPwd, ...payload } = form
      // 空字符串交给后端处理为 null，避免唯一索引冲突
      await register(payload)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #46c1b3 0%, #65caaf 45%, #59aaa4 100%);
  padding: 32px 16px;
}
.register-card {
  width: 820px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 32px 40px;
  box-shadow: 0 24px 60px rgba(6, 40, 80, 0.35);
}
.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
.brand-logo {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-header h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
}
.sub-title {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
