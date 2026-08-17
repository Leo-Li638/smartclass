<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card clickable" @click="$router.push(card.path)">
          <div class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-card-label">
            {{ card.label }}
            <el-icon class="stat-card-arrow"><Right /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>近 14 天每日正确率</template>
          <div ref="trendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>各学科正确率</template>
          <div ref="subjectRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-button link type="primary" @click="$router.push('/student/notices')">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="!notices.length" description="暂无公告" />
          <div v-for="notice in notices.slice(0, 3)" :key="notice.id" class="notice-item">
            <div class="notice-title">
              <el-tag size="small" :type="notice.targetRole === 'STUDENT' ? 'success' : 'primary'">
                {{ targetLabel(notice.targetRole) }}
              </el-tag>
              {{ notice.title }}
            </div>
            <p class="notice-content">{{ notice.content }}</p>
            <div class="notice-time">{{ notice.createTime }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Right } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/student'
import { getNoticesForMe } from '../../api/admin'

const data = ref(null)
const notices = ref([])

const statCards = computed(() => [
  { label: '累计练习(题)', value: data.value?.totalPractice ?? 0, color: '#409eff', path: '/student/practice' },
  { label: '总正确率', value: (data.value?.correctRate ?? 0) + '%', color: '#67c23a', path: '/student/recommend' },
  { label: '待巩固错题', value: data.value?.wrongCount ?? 0, color: '#f56c6c', path: '/student/wrong-book' },
  { label: '待完成作业', value: data.value?.unfinishedHomework ?? 0, color: '#e6a23c', path: '/student/homeworks' }
])

const trendRef = ref()
const subjectRef = ref()

onMounted(async () => {
  const [dashboardRes, noticeRes] = await Promise.all([getDashboard(), getNoticesForMe()])
  data.value = dashboardRes.data
  notices.value = noticeRes.data || []

  const trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>正确率: {c}%' },
    grid: { left: 45, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: (data.value.recentCorrectRate || []).map(i => i.name) },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#409eff' },
      data: (data.value.recentCorrectRate || []).map(i => i.value)
    }]
  })

  const subjectChart = echarts.init(subjectRef.value)
  subjectChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>正确率: {c}%' },
    grid: { left: 45, right: 20, top: 20, bottom: 60 },
    xAxis: {
      type: 'category',
      data: (data.value.subjectCorrectRate || []).map(i => i.name),
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'bar',
      barWidth: 22,
      itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
      data: (data.value.subjectCorrectRate || []).map(i => i.value)
    }]
  })

  window.addEventListener('resize', () => {
    trendChart.resize()
    subjectChart.resize()
  })
})

function targetLabel(role) {
  return { ALL: '全体', TEACHER: '教师', STUDENT: '学生' }[role] || role
}
</script>

<style scoped>
.stat-card.clickable {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.stat-card-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card-label {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-card-arrow {
  font-size: 12px;
  opacity: 0.7;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px dashed #e4e7ed;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-content {
  font-size: 13px;
  color: #606266;
  margin: 8px 0;
  line-height: 1.6;
}

.notice-time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
