<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="calling-card">
          <div class="calling-label">当前叫号</div>
          <div class="calling-number">{{ callingItem ? callingItem.queue_number : '--' }}</div>
          <div class="calling-name">{{ callingItem ? callingItem.patient_name : '暂无呼叫' }}</div>
          <div class="calling-meta" v-if="callingItem">
            {{ callingItem.time_slot }} · {{ callingItem.appointment_no }}
          </div>
          <div class="calling-actions">
            <el-button type="primary" size="large" :loading="calling" @click="handleCallNext">
              呼叫下一位
            </el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="mt-16">
          <div class="mini-stat">
            <span>等待中</span>
            <strong>{{ waitingCount }}</strong>
          </div>
          <div class="mini-stat">
            <span>队列总数</span>
            <strong>{{ queue.length }}</strong>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="15">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="page-title">今日候诊队列</span>
              <el-button text type="primary" :loading="loading" @click="loadQueue">刷新</el-button>
            </div>
          </template>

          <el-table v-loading="loading" :data="queue" stripe style="width: 100%">
            <el-table-column prop="queue_number" label="号码" width="80" />
            <el-table-column prop="patient_name" label="患者" width="110" />
            <el-table-column prop="patient_phone" label="电话" width="130" />
            <el-table-column prop="time_slot" label="时段" width="130" />
            <el-table-column prop="symptoms" label="症状" min-width="140" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'calling' ? 'primary' : 'warning'" size="small">
                  {{ row.status === 'calling' ? '叫号中' : '等待中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'waiting'"
                  type="danger"
                  link
                  @click="handleSkip(row)"
                >
                  跳过
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && queue.length === 0" description="当前没有候诊患者" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getQueueStatus, callNextPatient, skipPatient } from '@/apis/patientQueue'

const userStore = useUserStore()

const loading = ref(false)
const calling = ref(false)
const queue = ref([])
let timer = null

const callingItem = computed(() => queue.value.find((item) => item.status === 'calling') || null)
const waitingCount = computed(() => queue.value.filter((item) => item.status === 'waiting').length)

const loadQueue = async () => {
  if (!userStore.userId) return
  loading.value = true
  try {
    queue.value = (await getQueueStatus(userStore.userId)) || []
  } finally {
    loading.value = false
  }
}

const handleCallNext = async () => {
  calling.value = true
  try {
    const result = await callNextPatient(userStore.userId)
    if (result?.success) {
      ElMessage.success(`请 ${result.patientName}（${result.queueNumber}号）就诊`)
    } else {
      ElMessage.warning(result?.message || '当前没有可呼叫的患者')
    }
    await loadQueue()
  } finally {
    calling.value = false
  }
}

const handleSkip = async (row) => {
  try {
    await ElMessageBox.confirm(`确认跳过 ${row.patient_name}（${row.queue_number}号）？`, '跳过患者', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await skipPatient(row.id)
  ElMessage.success('已跳过')
  await loadQueue()
}

onMounted(() => {
  loadQueue()
  timer = setInterval(loadQueue, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.calling-card {
  text-align: center;
  background: linear-gradient(160deg, #1976d2 0%, #26a69a 100%);
  color: #fff;
  border: none;
}
.calling-card :deep(.el-card__body) {
  padding: 32px 20px;
}
.calling-label {
  font-size: 13px;
  opacity: 0.85;
}
.calling-number {
  font-size: 64px;
  font-weight: 700;
  line-height: 1.1;
  margin: 8px 0;
}
.calling-name {
  font-size: 18px;
}
.calling-meta {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}
.calling-actions {
  margin-top: 24px;
}
.mini-stat {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  color: #606266;
}
.mini-stat strong {
  font-size: 22px;
  color: #1976d2;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
