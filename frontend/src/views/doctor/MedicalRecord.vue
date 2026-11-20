<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="page-title">今日接诊列表</span>
              <el-button text type="primary" :loading="loading" @click="loadAppointments">刷新</el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="appointments"
            highlight-current-row
            style="width: 100%"
            @current-change="handleSelect"
          >
            <el-table-column label="患者" width="100">
              <template #default="{ row }">{{ row.patientName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="timeSlot" label="时段" width="120" />
            <el-table-column label="排队号" width="90">
              <template #default="{ row }">{{ row.queueNumber ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && appointments.length === 0" description="今日暂无待接诊预约" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="page-title">填写病历</span>
              <div class="header-tools" v-if="selected">
                <el-tag type="success" effect="plain">当前患者：{{ selected.patientName }}</el-tag>
                <el-button text type="primary" @click="showPatientProfile(selected)">患者资料</el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!selected" description="请先在左侧选择一位接诊患者" />

          <template v-else>
            <el-descriptions :column="2" border size="small" class="mb-16">
              <el-descriptions-item label="预约单号">{{ selected.appointmentNo }}</el-descriptions-item>
              <el-descriptions-item label="就诊时段">{{ selected.timeSlot }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ selected.patientPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="排队号">{{ selected.queueNumber ?? '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-form :model="form" label-width="80px" :disabled="Boolean(currentRecord)">
              <el-form-item label="症状描述">
                <el-input v-model="form.symptoms" type="textarea" :rows="2" placeholder="患者症状" />
              </el-form-item>
              <el-form-item label="诊断结果">
                <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断结果" />
              </el-form-item>
              <el-form-item label="医嘱建议">
                <el-input v-model="form.advice" type="textarea" :rows="3" placeholder="请输入医嘱建议" />
              </el-form-item>
            </el-form>

            <div class="actions" v-if="!currentRecord">
              <el-button type="primary" :loading="submitting" @click="handleCreateRecord">保存病历</el-button>
            </div>

            <template v-else>
              <el-divider content-position="left">病历图片</el-divider>
              <div class="case-upload-head">
                <el-upload
                  action="#"
                  :show-file-list="false"
                  :http-request="handleCaseImageUpload"
                  accept=".png,.jpg,.jpeg,.gif,.webp"
                  :disabled="caseImages.length >= 9"
                >
                  <el-button :disabled="caseImages.length >= 9">选择图片</el-button>
                </el-upload>
                <span class="upload-tip">上传至 OSS case 目录，每张不超过 5MB，最多 9 张</span>
              </div>
              <div v-if="caseImages.length" class="case-image-list">
                <div v-for="image in caseImages" :key="image.uid" class="case-image-item">
                  <el-image
                    v-if="image.status === 'success'"
                    class="case-image"
                    :src="image.url"
                    :preview-src-list="caseImageUrls"
                    :initial-index="caseImageUrls.indexOf(image.url)"
                    fit="cover"
                    preview-teleported
                  />
                  <div v-else class="case-image-placeholder">
                    <span v-if="image.status === 'uploading'">上传中...</span>
                    <template v-else>
                      <span>上传失败</span>
                      <el-button type="primary" link @click="retryCaseImage(image)">重试</el-button>
                    </template>
                  </div>
                  <div class="case-image-name" :title="image.name">{{ image.name }}</div>
                  <el-button
                    v-if="image.status === 'error'"
                    type="danger"
                    link
                    class="remove-image"
                    @click="removeCaseImage(image.uid)"
                  >移除</el-button>
                </div>
              </div>

              <el-divider content-position="left">处方明细</el-divider>
              <el-table :data="prescriptions" size="small" border style="width: 100%">
                <el-table-column label="药品名称" min-width="150">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.medicineName"
                      filterable
                      allow-create
                      default-first-option
                      placeholder="选择或输入药品"
                      style="width: 100%"
                      @change="() => handleDrugChange(row)"
                    >
                      <el-option v-for="drug in drugs" :key="drug.id" :label="drug.name" :value="drug.name" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="规格" width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.specification" placeholder="规格" />
                  </template>
                </el-table-column>
                <el-table-column label="用法用量" min-width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.dosage" placeholder="如：一日三次，每次一片" />
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="100">
                  <template #default="{ row }">
                    <el-input-number v-model="row.quantity" :min="1" :max="99" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="备注" min-width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.remark" placeholder="备注" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70">
                  <template #default="{ $index }">
                    <el-button type="danger" link @click="removePrescription($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="actions">
                <el-button @click="addPrescriptionRow">添加药品</el-button>
                <el-button type="primary" :loading="savingPrescription" @click="handleSavePrescriptions">
                  保存处方
                </el-button>
                <el-button v-if="selected.status !== 'completed'" type="success" @click="handleComplete">
                  完成就诊
                </el-button>
                <el-button @click="resetRecord">继续接诊下一位</el-button>
              </div>
            </template>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 患者资料卡（医生接诊时查看） -->
    <el-drawer v-model="profileVisible" title="患者资料" size="420px">
      <div v-loading="profileLoading">
        <div class="patient-head">
          <el-avatar :size="56" :src="patientProfile.avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div>
            <div class="patient-name">{{ patientProfile.realName || '-' }}</div>
            <div class="patient-sub">{{ patientProfile.username || '' }}</div>
          </div>
        </div>

        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="性别">{{ patientProfile.gender || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ patientProfile.birthDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ patientProfile.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ patientProfile.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ patientProfile.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系地址">{{ patientProfile.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ patientProfile.emergencyContact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系电话">
            {{ patientProfile.emergencyContactPhone || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">历史就诊记录</el-divider>
        <el-empty v-if="patientRecords.length === 0" description="暂无历史就诊记录" :image-size="60" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="record in patientRecords"
            :key="record.id"
            :timestamp="record.visit_time || record.visitTime"
          >
            <div class="record-title">{{ record.diagnosis || '-' }}</div>
            <div class="record-line">医生：{{ record.doctor_name || record.doctorName || '-' }}</div>
            <div class="record-line">医嘱：{{ record.advice || '-' }}</div>
            <div v-if="record.images && record.images.length" class="history-images">
              <el-image
                v-for="(url, index) in record.images"
                :key="url"
                class="history-image"
                :src="url"
                :preview-src-list="record.images"
                :initial-index="index"
                fit="cover"
                preview-teleported
              />
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentDoctor } from '@/apis/doctor'
import { filterAppointments, completeAppointment } from '@/apis/appointment'
import {
  createMedicalRecord,
  addPrescription,
  getPatientRecords,
  uploadMedicalRecordImage
} from '@/apis/medicalRecord'
import { getPatientProfile } from '@/apis/profile'
import { getAllDrugs } from '@/apis/drug'

const loading = ref(false)
const submitting = ref(false)
const savingPrescription = ref(false)

const doctor = ref(null)
const appointments = ref([])
const selected = ref(null)
const currentRecord = ref(null)
const prescriptions = ref([])
const drugs = ref([])
const caseImages = ref([])
const caseImageUrls = computed(() => caseImages.value
  .filter((image) => image.status === 'success')
  .map((image) => image.url))
const CASE_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif', 'image/webp']

const form = reactive({
  symptoms: '',
  diagnosis: '',
  advice: ''
})

const today = (() => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
})()

const statusText = (status) => {
  const map = { pending: '待支付', paid: '已支付', calling: '叫号中', completed: '已完成', cancelled: '已取消' }
  return map[status] || status || '-'
}

const statusType = (status) => {
  const map = { pending: 'warning', paid: 'success', calling: 'primary', completed: 'info', cancelled: 'danger' }
  return map[status] || 'info'
}

const loadAppointments = async () => {
  if (!doctor.value?.id) return
  loading.value = true
  try {
    const data = (await filterAppointments({
      doctorId: doctor.value.id,
      startDate: today,
      endDate: today
    })) || []
    appointments.value = data.filter((item) => ['paid', 'calling', 'completed'].includes(item.status))
  } finally {
    loading.value = false
  }
}

const handleSelect = (row) => {
  if (!row) return
  selected.value = row
  currentRecord.value = null
  prescriptions.value = []
  caseImages.value = []
  form.symptoms = row.symptoms || ''
  form.diagnosis = ''
  form.advice = ''
}

const handleCreateRecord = async () => {
  if (!selected.value) return
  if (!form.diagnosis.trim()) {
    ElMessage.warning('请填写诊断结果')
    return
  }
  submitting.value = true
  try {
    currentRecord.value = await createMedicalRecord({
      appointmentId: selected.value.id,
      symptoms: form.symptoms,
      diagnosis: form.diagnosis,
      advice: form.advice
    })
    prescriptions.value = [createEmptyPrescription()]
    ElMessage.success('病历已保存，可继续开具处方')
    await loadAppointments()
  } finally {
    submitting.value = false
  }
}

const handleCaseImageUpload = async ({ file }) => {
  if (!currentRecord.value?.id) return
  if (!CASE_IMAGE_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 png/jpg/jpeg/gif/webp 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('病历图片不能超过 5MB')
    return
  }
  if (caseImages.value.length >= 9) {
    ElMessage.warning('每份病历最多上传 9 张图片')
    return
  }

  const image = {
    uid: `${Date.now()}-${Math.random()}`,
    name: file.name,
    file,
    url: '',
    status: 'uploading'
  }
  caseImages.value.push(image)
  await uploadCaseImage(image)
}

const uploadCaseImage = async (image) => {
  image.status = 'uploading'
  try {
    const result = await uploadMedicalRecordImage(currentRecord.value.id, image.file)
    image.url = result.url
    image.status = 'success'
    ElMessage.success(`${image.name} 上传成功`)
  } catch (error) {
    // OSS 网络异常由后端自动重试；仍失败时保留文件，可在此手动重试。
    image.status = 'error'
  }
}

const retryCaseImage = (image) => uploadCaseImage(image)

const removeCaseImage = (uid) => {
  caseImages.value = caseImages.value.filter((image) => image.uid !== uid)
}

const createEmptyPrescription = () => ({
  medicineName: '',
  specification: '',
  dosage: '',
  quantity: 1,
  remark: ''
})

const addPrescriptionRow = () => {
  prescriptions.value.push(createEmptyPrescription())
}

const removePrescription = (index) => {
  prescriptions.value.splice(index, 1)
}

const handleDrugChange = (row) => {
  const drug = drugs.value.find((item) => item.name === row.medicineName)
  if (drug && !row.specification) {
    row.specification = drug.specification || ''
  }
}

const handleSavePrescriptions = async () => {
  if (!currentRecord.value?.id) return
  const targets = prescriptions.value.filter((row) => row.medicineName && row.medicineName.trim())
  if (targets.length === 0) {
    ElMessage.warning('请至少填写一条药品')
    return
  }
  savingPrescription.value = true
  try {
    for (const row of targets) {
      await addPrescription({
        medicalRecordId: currentRecord.value.id,
        medicineName: row.medicineName,
        specification: row.specification,
        dosage: row.dosage,
        quantity: row.quantity,
        remark: row.remark
      })
    }
    ElMessage.success('处方已保存')
  } finally {
    savingPrescription.value = false
  }
}

const handleComplete = async () => {
  if (!selected.value) return
  await completeAppointment(selected.value.id)
  ElMessage.success('本次就诊已完成')
  resetRecord()
  await loadAppointments()
}

const resetRecord = () => {
  selected.value = null
  currentRecord.value = null
  prescriptions.value = []
  caseImages.value = []
  form.symptoms = ''
  form.diagnosis = ''
  form.advice = ''
}

/* ---------- 患者资料卡 ---------- */
const profileVisible = ref(false)
const profileLoading = ref(false)
const patientProfile = ref({})
const patientRecords = ref([])

const showPatientProfile = async (row) => {
  if (!row?.patientId) {
    ElMessage.warning('缺少患者信息')
    return
  }
  profileVisible.value = true
  profileLoading.value = true
  try {
    patientProfile.value = (await getPatientProfile(row.patientId)) || {}
    patientRecords.value = (await getPatientRecords(row.patientId)) || []
  } finally {
    profileLoading.value = false
  }
}

onMounted(async () => {
  doctor.value = await getCurrentDoctor().catch(() => null)
  if (!doctor.value?.id) {
    ElMessage.error('当前账号没有关联医生信息')
    return
  }
  drugs.value = (await getAllDrugs().catch(() => [])) || []
  await loadAppointments()
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
.mb-16 {
  margin-bottom: 16px;
}
.header-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}
.patient-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
}
.patient-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}
.patient-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.record-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.record-line {
  margin-top: 4px;
  font-size: 13px;
  color: #606266;
}
.case-upload-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.upload-tip {
  color: #909399;
  font-size: 12px;
}
.case-image-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
}
.case-image-item {
  position: relative;
  width: 104px;
}
.case-image,
.case-image-placeholder {
  width: 104px;
  height: 104px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
}
.case-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}
.case-image-name {
  margin-top: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  font-size: 12px;
}
.remove-image {
  position: absolute;
  top: 76px;
  right: 2px;
  padding: 0 4px;
  background: rgba(255, 255, 255, 0.9);
}
.history-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.history-image {
  width: 64px;
  height: 64px;
  border-radius: 6px;
}
.actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
