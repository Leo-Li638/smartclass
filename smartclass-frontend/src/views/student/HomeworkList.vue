<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>作业中心</span>
          <el-radio-group v-model="statusFilter" @change="applyFilter">
            <el-radio-button :value="-1">全部</el-radio-button>
            <el-radio-button :value="0">待完成</el-radio-button>
            <el-radio-button :value="1">待批阅</el-radio-button>
            <el-radio-button :value="2">已批阅</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="课程" width="130" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="teacherName" label="布置教师" width="90" />
        <el-table-column prop="questionCount" label="题数" width="70" align="center" />
        <el-table-column prop="totalScore" label="总分" width="70" align="center" />
        <el-table-column label="截止时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'overdue': isOverdue(row) }">{{ formatTime(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="我的状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.myStatus)">{{ statusText(row.myStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.myStatus > 0" class="score">{{ row.myScore }} / {{ row.totalScore }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.myStatus === 0"
              size="small"
              type="primary"
              :disabled="isOverdue(row)"
              @click="$router.push(`/student/homeworks/${row.id}`)"
            >
              去作答
            </el-button>
            <el-button v-else link type="primary" size="small" @click="$router.push(`/student/homeworks/${row.id}`)">
              查看成绩
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!records.length && !loading" description="暂无作业" />

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getHomeworks } from '../../api/student'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const statusFilter = ref(-1)

const query = reactive({ current: 1, size: 10 })

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getHomeworks({ current: query.current, size: query.size })
    const page = res.data
    total.value = page.total
    applyClientFilter(page.records || [])
  } finally {
    loading.value = false
  }
}

function applyClientFilter(list) {
  records.value = statusFilter.value === -1
    ? list
    : list.filter(item => item.myStatus === statusFilter.value)
}

function applyFilter() {
  loadData()
}

function isOverdue(row) {
  if (!row.endTime) return false
  return new Date(row.endTime).getTime() < Date.now()
}

function statusText(status) {
  return { 0: '待完成', 1: '待批阅', 2: '已批阅' }[status] || '未知'
}

function statusTag(status) {
  return { 0: 'warning', 1: 'primary', 2: 'success' }[status] || 'info'
}

function formatTime(time) {
  if (!time) return '不限时'
  return String(time).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overdue {
  color: #f56c6c;
}

.score {
  color: #409eff;
  font-weight: 600;
}
</style>
