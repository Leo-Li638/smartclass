<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-card-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>近 7 天平台做题量趋势</template>
          <div ref="trendChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>各学科题库分布</template>
          <div ref="subjectChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>各班级学生人数</template>
          <div ref="clazzChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/admin'

const data = ref(null)

const statCards = computed(() => [
  { label: '教师人数', value: data.value?.teacherCount ?? 0, color: '#409eff' },
  { label: '学生人数', value: data.value?.studentCount ?? 0, color: '#67c23a' },
  { label: '开设课程', value: data.value?.courseCount ?? 0, color: '#e6a23c' },
  { label: '发布作业', value: data.value?.homeworkCount ?? 0, color: '#f56c6c' },
  { label: '题库题目', value: data.value?.questionCount ?? 0, color: '#909399' },
  { label: '累计练习人次', value: data.value?.practiceCount ?? 0, color: '#9254de' },
  { label: '班级数量', value: data.value?.clazzCount ?? 0, color: '#36cfc9' },
  { label: '系统状态', value: '正常运行', color: '#67c23a' }
])

const trendChartRef = ref()
const subjectChartRef = ref()
const clazzChartRef = ref()

onMounted(async () => {
  const res = await getDashboard()
  data.value = res.data

  const trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: (res.data.recentPractice || []).map(i => i.name.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#409eff' },
      data: (res.data.recentPractice || []).map(i => i.value)
    }]
  })

  const subjectChart = echarts.init(subjectChartRef.value)
  subjectChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['38%', '62%'],
      data: (res.data.subjectQuestionDist || []).map(i => ({ name: i.name, value: i.value }))
    }]
  })

  const clazzChart = echarts.init(clazzChartRef.value)
  clazzChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: (res.data.clazzStudentDist || []).map(i => i.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      barWidth: 40,
      itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
      data: (res.data.clazzStudentDist || []).map(i => i.value)
    }]
  })

  window.addEventListener('resize', () => {
    trendChart.resize()
    subjectChart.resize()
    clazzChart.resize()
  })
})
</script>
