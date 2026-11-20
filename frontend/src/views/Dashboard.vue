<template>
  <div class="page-container">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div>
          <h2>{{ greeting }}，{{ userStore.realName }}</h2>
          <p class="text-muted">
            今天是 {{ today }}，祝您工作顺利。
          </p>
        </div>
        <div class="quick-actions">
          <template v-if="userStore.hasRole('patient')">
            <el-button type="primary" @click="router.push('/appointment')">预约挂号</el-button>
            <el-button @click="router.push('/my-appointments')">我的预约</el-button>
          </template>
          <template v-else-if="userStore.hasRole('doctor')">
            <el-button type="primary" @click="router.push('/doctor/call-queue')">开始叫号</el-button>
            <el-button @click="router.push('/doctor/medical-record')">写病历</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/admin/doctors')">医生管理</el-button>
            <el-button @click="router.push('/admin/logs')">操作日志</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 患者：今日候诊实时进度 -->
    <el-card v-if="userStore.hasRole('patient') && queueInfo.hasQueue" shadow="never" class="mt-16 queue-card">
      <div class="queue-row">
        <div>
          <div class="queue-title">
            候诊进行中
            <el-tag v-if="queueInfo.queue_status === 'calling'" type="warning" size="small">请前往诊室</el-tag>
          </div>
          <div class="queue-sub">
            {{ queueInfo.department_name || '-' }} · {{ queueInfo.doctor_name || '-' }}
            <template v-if="queueInfo.doctor_title">（{{ queueInfo.doctor_title }}）</template>
            · {{ queueInfo.time_slot || '-' }}
          </div>
        </div>
        <div class="queue-items">
          <div class="queue-item">
            <div class="queue-label">当前叫号</div>
            <div class="queue-num">{{ queueInfo.calling_number ?? '-' }}</div>
          </div>
          <div class="queue-item">
            <div class="queue-label">我的号</div>
            <div class="queue-num mine">{{ queueInfo.queue_number }}</div>
          </div>
          <div class="queue-item">
            <div class="queue-label">前方等待</div>
            <div class="queue-num">{{ queueInfo.ahead_count ?? 0 }} 人</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="mt-16">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">科室数量</div>
          <div class="stat-value">{{ stats.departments }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">医生数量</div>
          <div class="stat-value">{{ stats.doctors }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ userStore.hasRole('patient') ? '我的预约' : '今日预约' }}</div>
          <div class="stat-value">{{ stats.appointments }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ userStore.hasRole('doctor') ? '当前候诊' : userStore.hasRole('patient') ? '已完成就诊' : '今日已支付' }}</div>
          <div class="stat-value">{{ stats.extra }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="card-title">
          <span>{{ tableTitle }}</span>
          <el-button text type="primary" :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" stripe style="width: 100%">
        <el-table-column prop="appointmentNo" label="预约单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="130" />
        <el-table-column v-if="!userStore.hasRole('patient')" label="患者" width="100">
          <template #default="{ row }">{{ row.patientName || '-' }}</template>
        </el-table-column>
        <el-table-column label="医生" width="110">
          <template #default="{ row }">{{ row.doctorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">￥{{ row.amount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="排队号" width="90">
          <template #default="{ row }">{{ row.queueNumber ?? '-' }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && rows.length === 0" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDepartmentList } from '@/apis/department'
import { getDoctorList, getCurrentDoctor } from '@/apis/doctor'
import { getMyAppointments, filterAppointments } from '@/apis/appointment'
import { getQueueStatus, getMyQueueStatus } from '@/apis/patientQueue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rows = ref([])
const queueInfo = ref({})

const stats = reactive({
  departments: 0,
  doctors: 0,
  appointments: 0,
  extra: 0
})

const today = new Date().toLocaleDateString('zh-CN')
const todayValue = (() => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
})()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const tableTitle = computed(() => {
  if (userStore.hasRole('patient')) return '我的预约'
  if (userStore.hasRole('doctor')) return '今日接诊预约'
  return '今日全院预约'
})

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
    const [departments, doctors] = await Promise.all([
      getDepartmentList(),
      getDoctorList()
    ])
    stats.departments = (departments || []).length
    stats.doctors = (doctors || []).length

    if (userStore.hasRole('patient')) {
      rows.value = (await getMyAppointments()) || []
      stats.appointments = rows.value.length
      stats.extra = rows.value.filter((item) => item.status === 'completed').length
      queueInfo.value = (await getMyQueueStatus().catch(() => ({}))) || {}
    } else if (userStore.hasRole('doctor')) {
      const doctor = await getCurrentDoctor().catch(() => null)
      if (doctor?.id) {
        rows.value = (await filterAppointments({
          doctorId: doctor.id,
          startDate: todayValue,
          endDate: todayValue
        })) || []
      } else {
        rows.value = []
      }
      stats.appointments = rows.value.length
      const queue = await getQueueStatus(userStore.userId).catch(() => [])
      stats.extra = (queue || []).filter((item) => item.status === 'waiting').length
    } else {
      rows.value = (await filterAppointments({ startDate: todayValue, endDate: todayValue })) || []
      stats.appointments = rows.value.length
      stats.extra = rows.value.filter((item) => item.status === 'paid' || item.status === 'calling').length
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.welcome-card {
  border-radius: 12px;
}
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.welcome h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1f2d3d;
}
.welcome p {
  margin: 0;
  font-size: 13px;
}
.quick-actions {
  display: flex;
  gap: 10px;
}
.stat-card {
  border-radius: 12px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #1976d2;
}
.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
.queue-card {
  border-left: 4px solid #e6a23c;
}
.queue-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.queue-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.queue-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.queue-items {
  display: flex;
  gap: 40px;
}
.queue-item {
  text-align: center;
}
.queue-label {
  font-size: 12px;
  color: #909399;
}
.queue-num {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.queue-num.mine {
  color: #409eff;
}
</style>
