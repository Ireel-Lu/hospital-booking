<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="page-title">操作日志</span>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              placeholder="搜索操作人 / 类型 / 内容"
              clearable
              style="width: 260px"
            />
            <el-button :loading="loading" @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="filteredLogs" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="170" />
        <el-table-column prop="targetType" label="目标类型" width="130" />
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column prop="content" label="操作内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP 地址" width="150" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <el-empty v-if="!loading && filteredLogs.length === 0" description="暂无操作日志" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getLogList } from '@/apis/log'

const loading = ref(false)
const keyword = ref('')
const logs = ref([])

const filteredLogs = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return logs.value
  return logs.value.filter((item) =>
    [item.operatorName, item.operationType, item.targetType, item.content]
      .filter(Boolean)
      .some((text) => String(text).toLowerCase().includes(kw))
  )
})

const loadData = async () => {
  loading.value = true
  try {
    logs.value = (await getLogList()) || []
  } finally {
    loading.value = false
  }
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
