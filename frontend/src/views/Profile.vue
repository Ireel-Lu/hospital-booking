<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>个人资料</span>
              <el-tag size="small" effect="plain">{{ roleText }}</el-tag>
            </div>
          </template>

          <div class="avatar-row">
            <el-upload
              action="#"
              :show-file-list="false"
              :http-request="handleUpload"
              accept=".png,.jpg,.jpeg,.gif,.webp"
            >
              <el-avatar :size="88" :src="form.avatar" class="avatar">
                <el-icon :size="34"><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-tip">{{ uploading ? '上传中...' : '点击更换头像' }}</div>
            </el-upload>
            <div class="account-meta">
              <div class="account-name">{{ form.realName || form.username }}</div>
              <div class="account-sub">{{ form.username }} · {{ roleText }}</div>
              <div v-if="form.departmentName" class="account-sub">
                {{ form.departmentName }}<template v-if="form.title"> · {{ form.title }}</template>
              </div>
              <div v-if="form.createTime" class="account-sub">注册时间：{{ form.createTime }}</div>
            </div>
          </div>

          <el-form :model="form" label-width="110px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="form.username" maxlength="30" placeholder="用于登录的账号" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="form.realName" maxlength="20" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>

            <template v-if="userStore.role === 'patient'">
              <el-form-item label="出生日期">
                <el-date-picker
                  v-model="form.birthDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择出生日期"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="身份证号">
                <el-input v-model="form.idCard" maxlength="18" />
              </el-form-item>
              <el-form-item label="联系地址">
                <el-input v-model="form.address" />
              </el-form-item>
              <el-form-item label="紧急联系人">
                <el-input v-model="form.emergencyContact" />
              </el-form-item>
              <el-form-item label="紧急联系电话">
                <el-input v-model="form.emergencyContactPhone" maxlength="11" />
              </el-form-item>
            </template>

            <template v-if="userStore.role === 'doctor'">
              <el-form-item label="工号">
                <el-input :model-value="form.employeeNo" disabled />
              </el-form-item>
              <el-form-item label="所属科室">
                <el-input :model-value="form.departmentName" disabled />
              </el-form-item>
              <el-form-item label="职称">
                <el-input v-model="form.title" />
              </el-form-item>
              <el-form-item label="专长">
                <el-input v-model="form.specialty" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="form.introduction" type="textarea" :rows="4" />
              </el-form-item>
              <el-form-item label="挂号费">
                <el-input :model-value="form.consultationFee" disabled />
              </el-form-item>
            </template>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header><span>修改密码</span></template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
            <el-form-item label="原密码" prop="oldPwd">
              <el-input v-model="pwdForm.oldPwd" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPwd">
              <el-input v-model="pwdForm.newPwd" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="pwdForm.confirmPwd" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="handleChangePwd">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
          <el-alert type="info" :closable="false" show-icon title="修改密码后需要重新登录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, changePassword, uploadAvatar } from '@/apis/profile'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  id: null,
  username: '',
  realName: '',
  gender: '',
  phone: '',
  email: '',
  avatar: '',
  role: '',
  createTime: '',
  birthDate: '',
  idCard: '',
  address: '',
  emergencyContact: '',
  emergencyContactPhone: '',
  employeeNo: '',
  title: '',
  specialty: '',
  introduction: '',
  departmentName: '',
  consultationFee: ''
})

const saving = ref(false)
const uploading = ref(false)

const pwdFormRef = ref()
const pwdSaving = ref(false)
const pwdForm = reactive({ oldPwd: '', newPwd: '', confirmPwd: '' })
const pwdRules = {
  oldPwd: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPwd) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleText = { patient: '患者', doctor: '医生', admin: '管理员' }[userStore.role] || '用户'

const loadProfile = async () => {
  const data = await getProfile()
  Object.keys(form).forEach((key) => {
    if (data[key] !== undefined && data[key] !== null) {
      form[key] = data[key]
    }
  })
}

const ALLOWED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif', 'image/webp']

const handleUpload = async ({ file }) => {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 png/jpg/jpeg/gif/webp 格式')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像不能超过 2MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadAvatar(file)
    form.avatar = url
    userStore.updateUserInfo({ avatar: url })
    ElMessage.success('头像已更新')
  } finally {
    uploading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const data = await updateProfile({
      username: form.username,
      realName: form.realName,
      gender: form.gender,
      phone: form.phone,
      email: form.email,
      birthDate: form.birthDate,
      idCard: form.idCard,
      address: form.address,
      emergencyContact: form.emergencyContact,
      emergencyContactPhone: form.emergencyContactPhone,
      title: form.title,
      specialty: form.specialty,
      introduction: form.introduction
    })
    Object.keys(form).forEach((key) => {
      if (data[key] !== undefined && data[key] !== null) {
        form[key] = data[key]
      }
    })
    userStore.updateUserInfo({
      username: form.username,
      realName: form.realName,
      gender: form.gender,
      avatar: form.avatar
    })
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

const handleChangePwd = async () => {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await changePassword({ oldPwd: pwdForm.oldPwd, newPwd: pwdForm.newPwd })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.logout()
    router.replace('/login')
  } finally {
    pwdSaving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 4px 0 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}
.avatar {
  background-color: #40fff9;
  color: #fff;
  cursor: pointer;
}
.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
.account-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.account-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.profile-form {
  max-width: 620px;
}
</style>
