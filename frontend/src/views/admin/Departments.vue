<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">科室管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索科室名称 / 编码"
              clearable
              style="width: 220px"
              @change="loadData"
            />
            <el-button type="primary" @click="openDialog()">新增科室</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="departments" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="科室名称" min-width="130" />
        <el-table-column prop="code" label="编码" width="110" />
        <el-table-column prop="location" label="位置" min-width="130" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && departments.length === 0" description="暂无科室" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑科室' : '新增科室'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" placeholder="如：内科" />
        </el-form-item>
        <el-form-item label="科室编码" prop="code">
          <el-input v-model="form.code" placeholder="如：NK" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="如：门诊楼 2 层" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="科室电话" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="科室描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="科室简介" />
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
import {
  getDepartmentList,
  addDepartment,
  updateDepartment,
  deleteDepartment
} from '@/apis/department'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const departments = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  location: '',
  phone: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入科室编码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    departments.value = (await getDepartmentList(keyword.value || undefined)) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    code: row?.code || '',
    description: row?.description || '',
    location: row?.location || '',
    phone: row?.phone || '',
    sortOrder: row?.sortOrder ?? 0,
    status: row?.status ?? 1
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await updateDepartment({ ...form })
        ElMessage.success('科室已更新')
      } else {
        const { id, ...payload } = form
        await addDepartment(payload)
        ElMessage.success('科室已新增')
      }
      dialogVisible.value = false
      await loadData()
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除科室「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDepartment(row.id)
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
