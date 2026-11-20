<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">排班号源设置</span>
          <div class="header-actions">
            <span class="text-muted" v-if="doctor">{{ doctor.name }} · {{ doctor.title }}</span>
            <el-date-picker
              v-model="date"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              @change="loadSettings"
            />
          </div>
        </div>
      </template>

      <el-alert
        title="每个时段设置总号源数量，保存后立即生效；已预约的号源不会被覆盖。"
        type="info"
        :closable="false"
        show-icon
        class="mb-16"
      />

      <el-table v-loading="loading" :data="rows" border style="width: 100%">
        <el-table-column prop="timeSlot" label="时段" width="160" />
        <el-table-column label="已配置总数" width="120">
          <template #default="{ row }">{{ row.savedTotal ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="剩余号源" width="120">
          <template #default="{ row }">{{ row.savedRemaining ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="设置总号源" min-width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.totalQuantity" :min="0" :max="200" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.savedTotal === null" type="info" size="small">未配置</el-tag>
            <el-tag v-else-if="(row.savedRemaining || 0) > 0" type="success" size="small">可预约</el-tag>
            <el-tag v-else type="danger" size="small">已约满</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-button @click="loadSettings">重置</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存全部</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentDoctor, getSourceSetting, saveSourceSetting } from '@/apis/doctor'

const TIME_SLOTS = [
  '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

const doctor = ref(null)
const date = ref(formatDate(new Date()))
const rows = ref([])
const loading = ref(false)
const saving = ref(false)

function formatDate(d) {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

function disabledDate(d) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d.getTime() < today.getTime()
}

const buildRows = (settings) => {
  return TIME_SLOTS.map((slot) => {
    const saved = (settings || []).find((item) => item.timeSlot === slot)
    return {
      timeSlot: slot,
      savedTotal: saved ? saved.totalQuantity : null,
      savedRemaining: saved ? saved.remainingQuantity : null,
      totalQuantity: saved ? saved.totalQuantity : 0
    }
  })
}

const loadSettings = async () => {
  if (!doctor.value?.id || !date.value) return
  loading.value = true
  try {
    const settings = await getSourceSetting({ doctorId: doctor.value.id, date: date.value })
    rows.value = buildRows(settings)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!doctor.value?.id) return
  const targets = rows.value.filter((row) => row.totalQuantity > 0)
  if (targets.length === 0) {
    ElMessage.warning('请至少为一个时段设置号源数量')
    return
  }
  saving.value = true
  try {
    for (const row of targets) {
      await saveSourceSetting({
        doctorId: doctor.value.id,
        date: date.value,
        timeSlot: row.timeSlot,
        totalQuantity: row.totalQuantity
      })
    }
    ElMessage.success('号源设置已保存')
    await loadSettings()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  doctor.value = await getCurrentDoctor().catch(() => null)
  if (!doctor.value?.id) {
    ElMessage.error('当前账号没有关联医生信息')
    return
  }
  await loadSettings()
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
  gap: 12px;
}
.mb-16 {
  margin-bottom: 16px;
}
.footer {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
