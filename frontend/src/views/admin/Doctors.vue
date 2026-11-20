<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">医生管理</span>
          <div class="header-actions">
            <el-select v-model="departmentId" placeholder="全部科室" clearable style="width: 180px" @change="loadDoctors">
              <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
            </el-select>
            <el-input
              v-model="keyword"
              placeholder="搜索医生姓名 / 职称 / 专长"
              clearable
              style="width: 240px"
              @change="loadDoctors"
            />
            <el-button type="primary" @click="openDialog()">新增医生</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="doctors" stripe style="width: 100%">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="specialty" label="专长" min-width="200" show-overflow-tooltip />
        <el-table-column label="挂号费" width="100">
          <template #default="{ row }">￥{{ row.consultationFee ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '在岗' : '停用' }}
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

      <el-empty v-if="!loading && doctors.length === 0" description="暂无医生" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑医生' : '新增医生'" width="560px">
      <el-alert
        v-if="!form.id"
        title="新增医生会同时在用户表创建登录账号，账号、密码可留空（密码默认 123456）"
        type="info"
        :closable="false"
        show-icon
        class="mb-16"
      />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="医生姓名" />
        </el-form-item>
        <template v-if="!form.id">
          <el-form-item label="登录账号">
            <el-input v-model="form.username" placeholder="留空自动生成 doc+时间戳" />
          </el-form-item>
          <el-form-item label="初始密码">
            <el-input v-model="form.pwd" placeholder="默认 123456" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" maxlength="11" placeholder="医生联系电话" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio value="男">男</el-radio>
              <el-radio value="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <el-form-item label="职称" prop="title">
          <el-select v-model="form.title" placeholder="选择职称" style="width: 100%">
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="住院医师" value="住院医师" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="选择科室" style="width: 100%">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="挂号费">
          <el-input-number v-model="form.consultationFee" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="擅长">
          <el-input v-model="form.specialty" type="textarea" :rows="2" placeholder="如：高血压、糖尿病诊治" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="医生简介" />
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
import { getDepartmentList } from '@/apis/department'
import { getDoctorList, addDoctor, updateDoctor, deleteDoctor } from '@/apis/doctor'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const departmentId = ref(null)
const departments = ref([])
const doctors = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  title: '',
  departmentId: null,
  consultationFee: 0,
  specialty: '',
  introduction: '',
  username: '',
  pwd: '',
  phone: '',
  gender: '男'
})

const rules = {
  name: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const loadDoctors = async () => {
  loading.value = true
  try {
    doctors.value = (await getDoctorList({
      departmentId: departmentId.value || undefined,
      keyword: keyword.value || undefined
    })) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    title: row?.title || '',
    departmentId: row?.departmentId ?? null,
    consultationFee: Number(row?.consultationFee ?? 0),
    specialty: row?.specialty || '',
    introduction: row?.introduction || '',
    username: '',
    pwd: '',
    phone: '',
    gender: '男'
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
        const result = await updateDoctor({ ...form })
        if (result?.success === false) {
          ElMessage.error(result.message || '更新失败')
          return
        }
        ElMessage.success('医生信息已更新')
      } else {
        const { id, ...payload } = form
        const result = await addDoctor(payload)
        if (result?.success === false) {
          ElMessage.error(result.message || '新增失败')
          return
        }
        ElMessageBox.alert(
          `登录账号：<b>${result?.username || '-'}</b><br/>初始密码：<b>${result?.initialPassword || '123456'}</b>`,
          '医生账号已创建',
          { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' }
        )
      }
      dialogVisible.value = false
      await loadDoctors()
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除医生「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDoctor(row.id)
  ElMessage.success('已删除')
  await loadDoctors()
}

onMounted(async () => {
  departments.value = (await getDepartmentList()) || []
  await loadDoctors()
})
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
  flex-wrap: wrap;
}
.mb-16 {
  margin-bottom: 16px;
}
</style>
