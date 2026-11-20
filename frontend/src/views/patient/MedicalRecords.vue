<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">就诊记录</span>
          <el-button :loading="exporting" @click="handleExport">导出 Excel</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="records" stripe style="width: 100%">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="prescription-box">
              <div v-if="row.images && row.images.length" class="record-images">
                <div class="prescription-title">病历图片</div>
                <div class="image-grid">
                  <el-image
                    v-for="(url, index) in row.images"
                    :key="url"
                    class="record-image"
                    :src="url"
                    :preview-src-list="row.images"
                    :initial-index="index"
                    fit="cover"
                    preview-teleported
                  />
                </div>
              </div>
              <div class="prescription-title">处方明细</div>
              <el-table :data="prescriptions[row.id] || []" size="small" border>
                <el-table-column prop="medicine_name" label="药品名称" min-width="140" />
                <el-table-column prop="specification" label="规格" width="120" />
                <el-table-column prop="dosage" label="用法用量" min-width="140" />
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="remark" label="备注" min-width="140" />
              </el-table>
              <el-empty
                v-if="prescriptions[row.id] && prescriptions[row.id].length === 0"
                description="本次就诊没有处方"
                :image-size="60"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="visit_time" label="就诊时间" width="180" />
        <el-table-column prop="appointment_date" label="预约日期" width="120" />
        <el-table-column prop="time_slot" label="时段" width="130" />
        <el-table-column label="医生" width="150">
          <template #default="{ row }">
            {{ row.doctor_name || '-' }}
            <span class="text-muted" v-if="row.doctor_title">（{{ row.doctor_title }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="department_name" label="科室" width="120" />
        <el-table-column prop="symptoms" label="症状" min-width="160" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断结果" min-width="160" show-overflow-tooltip />
        <el-table-column prop="advice" label="医嘱" min-width="180" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="暂无就诊记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getPatientRecords, getPrescriptions } from '@/apis/medicalRecord'
import { downloadFile } from '@/utils/download'

const userStore = useUserStore()

const loading = ref(false)
const exporting = ref(false)
const records = ref([])
const prescriptions = ref({})

const loadData = async () => {
  loading.value = true
  try {
    records.value = (await getPatientRecords(userStore.userId)) || []
    // 逐条加载处方，展开时就有数据
    for (const record of records.value) {
      await loadPrescriptions(record)
    }
  } finally {
    loading.value = false
  }
}

const loadPrescriptions = async (record) => {
  if (prescriptions.value[record.id]) return
  const data = (await getPrescriptions(record.id)) || []
  prescriptions.value = { ...prescriptions.value, [record.id]: data }
}

const handleExport = async () => {
  exporting.value = true
  try {
    await downloadFile(`/export/medical-records/patient/${userStore.userId}`, {}, '我的就诊记录.xlsx')
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.prescription-box {
  padding: 12px 48px 4px;
}
.record-images {
  margin-bottom: 18px;
}
.image-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.record-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
.prescription-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}
</style>
