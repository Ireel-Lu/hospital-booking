<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名 / 姓名 / 手机号"
              clearable
              style="width: 240px"
              @change="loadData"
            />
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && users.length === 0" description="暂无用户" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="患者" value="patient" />
            <el-option label="医生" value="doctor" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, updateUser, resetPassword, deleteUser } from '@/apis/user'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const users = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  role: 'patient'
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleText = (role) => ({ patient: '患者', doctor: '医生', admin: '管理员' }[role] || role)
const roleType = (role) => ({ patient: '', doctor: 'success', admin: 'danger' }[role] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    users.value = (await getUserList({ keyword: keyword.value || undefined })) || []
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row) => {
  const result = await updateUserStatus({ id: row.id, status: row.status })
  if (result?.success === false) {
    ElMessage.error(result.message || '操作失败')
    row.status = row.status === 1 ? 0 : 1
    return
  }
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    phone: row.phone || '',
    role: row.role || 'patient'
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const result = await updateUser({
        id: form.id,
        realName: form.realName,
        phone: form.phone,
        role: form.role
      })
      if (result?.success === false) {
        ElMessage.error(result.message || '更新失败')
        return
      }
      ElMessage.success('用户信息已更新')
      dialogVisible.value = false
      await loadData()
    } finally {
      saving.value = false
    }
  })
}

const handleResetPassword = async (row) => {
  let pwd = ''
  try {
    const { value } = await ElMessageBox.prompt(`为「${row.username}」设置新密码`, '重置密码', {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      inputPlaceholder: '至少 6 位',
      inputValidator: (val) => (val && val.length >= 6 ? true : '密码至少 6 位')
    })
    pwd = value
  } catch (e) {
    return
  }
  const result = await resetPassword({ id: row.id, pwd })
  if (result?.success === false) {
    ElMessage.error(result.message || '重置失败')
    return
  }
  ElMessage.success('密码已重置')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteUser(row.id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
