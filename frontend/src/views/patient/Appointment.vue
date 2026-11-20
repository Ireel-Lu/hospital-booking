<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="step-title"><span class="step-index">1</span> 选择科室与医生</div>
      </template>

      <div class="filters">
        <el-select v-model="departmentId" placeholder="请选择科室" clearable style="width: 220px" @change="handleDepartmentChange">
          <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索医生姓名 / 职称 / 专长" clearable style="width: 260px" @change="loadDoctors" />
      </div>

      <div v-loading="doctorLoading" class="doctor-list">
        <el-empty v-if="!doctorLoading && doctors.length === 0" description="暂无可预约医生" />
        <div
          v-for="doctor in doctors"
          :key="doctor.id"
          class="doctor-card"
          :class="{ active: doctorId === doctor.id }"
          @click="handleDoctorChange(doctor.id)"
        >
          <el-avatar :size="52" class="doctor-avatar">{{ doctor.name?.slice(0, 1) }}</el-avatar>
          <div class="doctor-info">
            <div class="doctor-name">
              {{ doctor.name }}
              <el-tag size="small" type="success" effect="plain">{{ doctor.title }}</el-tag>
            </div>
            <div class="doctor-specialty">{{ doctor.specialty || '暂无专长介绍' }}</div>
            <div class="doctor-meta">
              <span>{{ doctor.departmentName || '未分配科室' }}</span>
              <span class="fee">挂号费 ￥{{ doctor.consultationFee ?? 0 }}</span>
            </div>
          </div>
          <el-icon v-if="doctorId === doctor.id" class="selected-icon"><CircleCheckFilled /></el-icon>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="step-title"><span class="step-index">2</span> 选择就诊日期与时段</div>
      </template>

      <el-date-picker
        v-model="date"
        type="date"
        placeholder="选择就诊日期"
        value-format="YYYY-MM-DD"
        :disabled-date="disabledDate"
        :disabled="!doctorId"
        @change="loadSlots"
      />

      <div v-loading="slotLoading" class="slot-list">
        <el-empty v-if="!slotLoading && slots.length === 0" description="该日期暂无号源，请选择其他日期" />
        <div
          v-for="slot in slots"
          :key="slot.id"
          class="slot-item"
          :class="{
            active: sourceSettingId === slot.id,
            disabled: slot.remainingQuantity <= 0
          }"
          @click="selectSlot(slot)"
        >
          <div class="slot-time">{{ slot.timeSlot }}</div>
          <div class="slot-remain">
            {{ slot.remainingQuantity > 0 ? `剩余 ${slot.remainingQuantity} 个` : '已约满' }}
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="step-title"><span class="step-index">3</span> 填写症状并提交</div>
      </template>

      <el-form label-width="90px">
        <el-form-item label="症状描述">
          <el-input
            v-model="symptoms"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请简单描述您的症状，便于医生提前了解病情"
          />
        </el-form-item>
      </el-form>

      <div class="submit-bar">
        <span class="text-muted">
          已选：{{ selectedDoctorName || '未选择医生' }}
          <template v-if="selectedSlot">· {{ selectedSlot.timeSlot }}</template>
          <template v-if="date">· {{ date }}</template>
        </span>
        <el-button type="primary" size="large" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
          提交预约
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getDepartmentList } from '@/apis/department'
import { getDoctorList, getSourceSetting } from '@/apis/doctor'
import { createAppointment, payAppointment } from '@/apis/appointment'

const router = useRouter()
const userStore = useUserStore()

const departments = ref([])
const departmentId = ref(null)
const keyword = ref('')
const doctors = ref([])
const doctorLoading = ref(false)

const date = ref(formatDate(new Date()))
const slots = ref([])
const slotLoading = ref(false)
const sourceSettingId = ref(null)
const symptoms = ref('')
const submitting = ref(false)

const doctorId = ref(null)

const selectedDoctor = computed(() => doctors.value.find((item) => item.id === doctorId.value))
const selectedDoctorName = computed(() => selectedDoctor.value?.name || '')
const selectedSlot = computed(() => slots.value.find((item) => item.id === sourceSettingId.value))
const canSubmit = computed(() => Boolean(doctorId.value && sourceSettingId.value))

function formatDate(d) {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

function disabledDate(d) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d.getTime() < today.getTime()
}

const loadDepartments = async () => {
  departments.value = (await getDepartmentList()) || []
}

const loadDoctors = async () => {
  doctorLoading.value = true
  try {
    doctors.value = (await getDoctorList({
      departmentId: departmentId.value || undefined,
      keyword: keyword.value || undefined
    })) || []
  } finally {
    doctorLoading.value = false
  }
}

const handleDepartmentChange = async () => {
  doctorId.value = null
  sourceSettingId.value = null
  slots.value = []
  await loadDoctors()
}

const handleDoctorChange = async (id) => {
  doctorId.value = id
  sourceSettingId.value = null
  await loadSlots()
}

const loadSlots = async () => {
  if (!doctorId.value || !date.value) return
  slotLoading.value = true
  try {
    slots.value = (await getSourceSetting({ doctorId: doctorId.value, date: date.value })) || []
  } finally {
    slotLoading.value = false
  }
}

const selectSlot = (slot) => {
  if (slot.remainingQuantity <= 0) return
  sourceSettingId.value = slot.id
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请先选择医生和就诊时段')
    return
  }
  submitting.value = true
  try {
    const appointment = await createAppointment({
      patientId: userStore.userId,
      doctorId: doctorId.value,
      sourceSettingId: sourceSettingId.value,
      symptoms: symptoms.value
    })

    try {
      await ElMessageBox.confirm(
        `预约成功，挂号费 ￥${appointment?.amount ?? 0}，是否立即支付？`,
        '支付提示',
        { confirmButtonText: '立即支付', cancelButtonText: '稍后支付', type: 'success' }
      )
      await payAppointment(appointment.id)
      ElMessage.success('支付成功，已生成候诊号')
      router.push('/my-appointments')
    } catch (e) {
      ElMessage.info('预约已保存，可在“我的预约”中继续支付')
      router.push('/my-appointments')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
})
</script>

<style scoped>
.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.step-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}
.step-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.doctor-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}
.doctor-card {
  position: relative;
  display: flex;
  gap: 14px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.doctor-card:hover {
  border-color: #409eff;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.14);
}
.doctor-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.doctor-avatar {
  background: #409eff;
  color: #fff;
  flex-shrink: 0;
}
.doctor-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
.doctor-specialty {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
  min-height: 20px;
}
.doctor-meta {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}
.fee {
  color: #e6a23c;
  font-weight: 600;
}
.selected-icon {
  position: absolute;
  right: 12px;
  top: 12px;
  color: #409eff;
  font-size: 18px;
}
.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}
.slot-item {
  width: 150px;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.slot-item:hover {
  border-color: #409eff;
}
.slot-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.slot-item.disabled {
  cursor: not-allowed;
  background: #f5f7fa;
  color: #c0c4cc;
}
.slot-time {
  font-weight: 600;
}
.slot-remain {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
</style>
