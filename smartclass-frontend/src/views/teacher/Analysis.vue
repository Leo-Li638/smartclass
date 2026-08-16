<template>
  <div class="page-container">
    <el-row :gutter="16" class="mb16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>所带班级成绩概览</template>
          <div v-if="!overview.length" class="empty-tip">暂无作业提交数据</div>
          <el-table v-else :data="overview" stripe>
            <el-table-column prop="clazzName" label="班级" width="140" />
            <el-table-column prop="submitCount" label="累计提交" width="100" />
            <el-table-column prop="avgScore" label="平均分" width="100" />
            <el-table-column label="及格率" min-width="200">
              <template #default="{ row }">
                <el-progress :percentage="Number(row.passRate) || 0" :color="progressColor(row.passRate)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>近期作业表现</template>
          <div v-if="!homeworks.length" class="empty-tip">暂无作业数据</div>
          <div v-else class="homework-list">
            <div v-for="hw in homeworks.slice(0, 8)" :key="hw.id" class="homework-item">
              <div class="hw-title">{{ hw.title }}</div>
              <div class="hw-meta">
                <span>{{ hw.clazzName }}</span>
                <span>提交 {{ hw.submitCount ?? 0 }} 份</span>
                <span>平均 {{ hw.avgScore ?? 0 }} / {{ hw.totalScore }} 分</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>近期作业平均得分率</template>
          <div ref="scoreRateRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>近期作业提交份数</template>
          <div ref="submitCountRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getClassOverview, getHomeworks } from '../../api/teacher'

const overview = ref([])
const homeworks = ref([])
const scoreRateRef = ref()
const submitCountRef = ref()

onMounted(async () => {
  const [overviewRes, homeworkRes] = await Promise.all([
    getClassOverview(),
    getHomeworks({ current: 1, size: 10 })
  ])
  overview.value = overviewRes.data || []
  homeworks.value = (homeworkRes.data?.records || []).filter(h => h.status === 1)

  await nextTick()
  renderScoreRateChart()
  renderSubmitCountChart()
})

function renderScoreRateChart() {
  if (!scoreRateRef.value) return
  const list = homeworks.value.slice(0, 8)
  const chart = echarts.init(scoreRateRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const i = list[params[0].dataIndex]
        return `${i.title}<br/>平均分:${i.avgScore ?? 0} / ${i.totalScore}<br/>得分率:${params[0].value}%`
      }
    },
    grid: { left: 45, right: 20, top: 20, bottom: 60 },
    xAxis: {
      type: 'category',
      data: list.map(i => i.title),
      axisLabel: { interval: 0, rotate: 18, fontSize: 11 }
    },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'bar',
      barWidth: 26,
      itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
      data: list.map(i => Math.round(((i.avgScore ?? 0) / (i.totalScore || 1)) * 100))
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderSubmitCountChart() {
  if (!submitCountRef.value) return
  const list = homeworks.value.slice(0, 8)
  const chart = echarts.init(submitCountRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const i = list[params[0].dataIndex]
        return `${i.title}<br/>提交:${params[0].value} 份`
      }
    },
    grid: { left: 45, right: 20, top: 20, bottom: 60 },
    xAxis: {
      type: 'category',
      data: list.map(i => i.title),
      axisLabel: { interval: 0, rotate: 18, fontSize: 11 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      barWidth: 26,
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      data: list.map(i => i.submitCount ?? 0)
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function progressColor(rate) {
  return rate >= 80 ? '#67c23a' : rate >= 60 ? '#e6a23c' : '#f56c6c'
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.empty-tip {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.homework-list {
  max-height: 420px;
  overflow-y: auto;
}

.homework-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 10px;
}

.hw-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.hw-meta {
  display: flex;
  gap: 14px;
  font-size: 13px;
  color: #909399;
}

.chart-box {
  width: 100%;
  height: 320px;
}
</style>
