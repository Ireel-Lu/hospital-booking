<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">我的预约</span>
          <div class="header-actions">
            <el-radio-group v-model="statusFilter" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="pending">待支付</el-radio-button>
              <el-radio-button value="paid">已支付</el-radio-button>
              <el-radio-button value="completed">已完成</el-radio-button>
              <el-radio-button value="cancelled">已取消</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="router.push('/appointment')">去预约</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="filteredList" stripe style="width: 100%">
        <el-table-column prop="appointmentNo" label="预约单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="130" />
        <el-table-column label="医生" width="140">
          <template #default="{ row }">
            {{ row.doctorName || '-' }}
            <span class="text-muted" v-if="row.doctorTitle">（{{ row.doctorTitle }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">￥{{ row.amount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排队号" width="90">
          <template #default="{ row }">
            {{ row.queueNumber ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="primary" link @click="handlePay(row)">支付</el-button>
            <el-button
              v-if="row.status === 'paid' || row.status === 'calling'"
              type="success"
              link
              @click="showQueue(row)"
            >
              候诊进度
            </el-button>
            <el-button v-if="row.status === 'paid'" type="danger" link @click="handleCancel(row)">取消</el-button>
            <el-button type="info" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredList.length === 0" description="暂无预约记录" />
    </el-card>

    <el-dialog v-model="detailVisible" title="预约详情" width="480px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="预约单号">{{ currentRow.appointmentNo }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ currentRow.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="就诊时段">{{ currentRow.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentRow.doctorName }} {{ currentRow.doctorTitle || '' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentRow.departmentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="症状描述">{{ currentRow.symptoms || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentRow.status) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 候诊进度 -->
    <el-dialog v-model="queueVisible" title="候诊进度" width="460px" @closed="stopPolling">
      <div v-if="queueInfo.hasQueue" class="queue-panel">
        <div class="queue-numbers">
          <div class="queue-item">
            <div class="queue-label">当前叫号</div>
            <div class="queue-value calling">{{ queueInfo.calling_number ?? '-' }}</div>
          </div>
          <div class="queue-item">
            <div class="queue-label">我的号</div>
            <div class="queue-value mine">{{ queueInfo.queue_number ?? '-' }}</div>
          </div>
          <div class="queue-item">
            <div class="queue-label">前方等待</div>
            <div class="queue-value">{{ queueInfo.ahead_count ?? 0 }} 人</div>
          </div>
        </div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="医生">
            {{ queueInfo.doctor_name }} {{ queueInfo.doctor_title || '' }}
          </el-descriptions-item>
          <el-descriptions-item label="科室">{{ queueInfo.department_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时段">{{ queueInfo.time_slot || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="queueInfo.queue_status === 'calling' ? 'primary' : 'info'">
              {{ queueInfo.queue_status === 'calling' ? '叫号中，请前往诊室' : '排队中' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="queue-tip">进度每 10 秒自动刷新</div>
      </div>
      <el-empty v-else description="今天暂无候诊队列（预约需为今天并已支付）" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAppointments, payAppointment, cancelAppointment } from '@/apis/appointment'
import { getMyQueueStatus } from '@/apis/patientQueue'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const statusFilter = ref('')
const detailVisible = ref(false)
const currentRow = ref(null)

const filteredList = computed(() =>
  statusFilter.value ? list.value.filter((item) => item.status === statusFilter.value) : list.value
)

const statusText = (status) => {
  const map = {
    pending: '待支付',
    paid: '已支付',
    calling: '叫号中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status || '-'
}

const statusType = (status) => {
  const map = {
    pending: 'warning',
    paid: 'success',
    calling: 'primary',
    completed: 'info',
    cancelled: 'danger'
  }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    list.value = (await getMyAppointments()) || []
  } finally {
    loading.value = false
  }
}

const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm(`确认支付挂号费 ￥${row.amount ?? 0}？`, '支付确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await payAppointment(row.id)
  ElMessage.success('支付成功，已生成候诊号')
  loadData()
}

const handleCancel = async (row) => {
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '例如：临时有事无法就诊'
    })
    reason = value
  } catch (e) {
    return
  }
  await cancelAppointment(row.id, reason)
  ElMessage.success('预约已取消')
  loadData()
}

const showDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

/* ---------- 候诊进度 ---------- */
const queueVisible = ref(false)
const queueInfo = ref({})
let queueTimer = null

const loadQueue = async () => {
  queueInfo.value = (await getMyQueueStatus()) || {}
}

const stopPolling = () => {
  if (queueTimer) {
    clearInterval(queueTimer)
    queueTimer = null
  }
}

const showQueue = async () => {
  queueVisible.value = true
  await loadQueue()
  stopPolling()
  queueTimer = setInterval(loadQueue, 10000)
}

onUnmounted(stopPolling)

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
  gap: 12px;
  flex-wrap: wrap;
}
.queue-numbers {
  display: flex;
  justify-content: space-around;
  padding: 12px 0 20px;
}
.queue-item {
  text-align: center;
}
.queue-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.queue-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}
.queue-value.calling {
  color: #e6a23c;
}
.queue-value.mine {
  color: #409eff;
}
.queue-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
