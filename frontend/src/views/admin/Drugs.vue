<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">药品管理</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索药品名称 / 厂家"
              clearable
              style="width: 220px"
              @change="handleSearch"
            />
            <el-button type="primary" @click="openDialog()">新增药品</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="records" stripe style="width: 100%">
        <el-table-column prop="name" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="130" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="180" show-overflow-tooltip />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">￥{{ row.price ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="category" label="类别" width="100" />
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

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="handleSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑药品' : '新增药品'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="form.name" placeholder="药品名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.specification" placeholder="如：0.5g*24片" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="form.manufacturer" placeholder="生产厂家" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="盒 / 瓶 / 片" />
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="form.category" placeholder="西药 / 中药 / 中成药" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="药品说明" />
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
import { getDrugList, addDrug, updateDrug, deleteDrug } from '@/apis/drug'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const records = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  specification: '',
  manufacturer: '',
  price: 0,
  stock: 0,
  unit: '',
  category: '',
  status: 1,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDrugList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined
    })
    records.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    specification: row?.specification || '',
    manufacturer: row?.manufacturer || '',
    price: Number(row?.price ?? 0),
    stock: row?.stock ?? 0,
    unit: row?.unit || '',
    category: row?.category || '',
    status: row?.status ?? 1,
    description: row?.description || ''
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
        await updateDrug({ ...form })
        ElMessage.success('药品已更新')
      } else {
        const { id, ...payload } = form
        await addDrug(payload)
        ElMessage.success('药品已新增')
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
    await ElMessageBox.confirm(`确认删除药品「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDrug(row.id)
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
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
