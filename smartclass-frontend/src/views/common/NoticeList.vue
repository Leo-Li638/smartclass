<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>系统公告</template>
      <div v-loading="loading">
        <div v-for="notice in notices" :key="notice.id" class="notice-item">
          <div class="notice-head">
            <el-tag size="small" :type="targetTag(notice.targetRole)">
              {{ targetLabel(notice.targetRole) }}
            </el-tag>
            <span class="notice-title">{{ notice.title }}</span>
            <span class="notice-time">{{ formatTime(notice.createTime) }}</span>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
        </div>
        <el-empty v-if="!notices.length && !loading" description="暂无公告" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getNoticesForMe } from '../../api/admin'

const loading = ref(false)
const notices = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNoticesForMe()
    notices.value = res.data || []
  } finally {
    loading.value = false
  }
})

function targetLabel(role) {
  return { ALL: '全体', TEACHER: '教师', STUDENT: '学生' }[role] || role
}

function targetTag(role) {
  return { ALL: 'primary', TEACHER: 'warning', STUDENT: 'success' }[role] || 'info'
}

function formatTime(time) {
  if (!time) return ''
  return String(time).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.notice-item {
  padding: 16px 4px;
  border-bottom: 1px dashed #e4e7ed;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
}

.notice-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.notice-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
