<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>提交情况</span>
              <el-tag>{{ submits.length }} 人已提交</el-tag>
            </div>
          </template>
          <el-table :data="submits" stripe v-loading="loading">
            <el-table-column prop="studentName" label="学生">
              <template #default="{ row }">{{ studentName(row.studentId) }}</template>
            </el-table-column>
            <el-table-column label="得分" width="100">
              <template #default="{ row }">
                <el-tag :type="scoreType(row)" size="small">{{ row.score }} / {{ row.totalScore }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="170" />
            <el-table-column label="批阅状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 2 ? 'success' : 'warning'" size="small">
                  {{ row.status === 2 ? '已批阅' : '待批阅' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewDetail(row)">查看明细</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>知识点错误率分析</template>
          <div v-if="!analysis.length" class="empty-tip">暂无提交数据</div>
          <div ref="chartRef" v-else class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailVisible" title="提交明细" width="760px" top="5vh">
      <div v-if="detail" class="detail-header">
        <span>学生:{{ detail.studentName }}({{ detail.clazzName }})</span>
        <el-tag type="primary" size="large">{{ detail.score }} / {{ detail.totalScore }} 分</el-tag>
      </div>
      <div v-if="detail" class="detail-questions">
        <div v-for="(answer, index) in detail.answers" :key="answer.questionId" class="answer-item">
          <div class="answer-title">
            <span>{{ index + 1 }}. {{ answer.title }}</span>
            <el-tag :type="answer.isCorrect === 1 ? 'success' : 'danger'" size="small">
              {{ answer.isCorrect === 1 ? '答对' : '答错' }} {{ answer.score }}/{{ answer.questionScore }}分
            </el-tag>
          </div>
          <div class="answer-line">学生答案:<b :class="answer.isCorrect === 1 ? 'text-success' : 'text-danger'">{{ answer.myAnswer || '(未作答)' }}</b></div>
          <div v-if="answer.isCorrect !== 1" class="answer-line">正确答案:<b class="text-success">{{ answer.rightAnswer }}</b></div>
          <div v-if="answer.analysis" class="answer-line analysis">解析:{{ answer.analysis }}</div>
        </div>
      </div>
      <div v-if="detail" style="margin-top: 12px">
        <el-input v-model="comment" type="textarea" :rows="2" placeholder="批阅评语(学生可见)" />
        <div style="margin-top: 10px; text-align: right">
          <el-button type="primary" :disabled="detail.status === 2 && !comment" @click="handleGrade">
            {{ detail.status === 2 ? '更新评语' : '确认批阅' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getSubmits, getSubmitDetail, gradeSubmit, getKnowledgeAnalysis, getStudents } from '../../api/teacher'

const route = useRoute()
const homeworkId = Number(route.params.homeworkId)
const loading = ref(false)
const submits = ref([])
const students = ref([])
const analysis = ref([])
const detailVisible = ref(false)
const detail = ref(null)
const comment = ref('')
const chartRef = ref()

onMounted(async () => {
  const [submitRes, studentRes, analysisRes] = await Promise.all([
    getSubmits(homeworkId),
    getStudents(),
    getKnowledgeAnalysis(homeworkId)
  ])
  submits.value = submitRes.data || []
  students.value = studentRes.data || []
  analysis.value = analysisRes.data || []

  if (analysis.value.length) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>错误率: {c}%' },
      grid: { left: 50, right: 20, top: 20, bottom: 60 },
      xAxis: {
        type: 'category',
        data: analysis.value.map(i => i.knowledgeName),
        axisLabel: { interval: 0, rotate: 20 }
      },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        type: 'bar',
        barWidth: 30,
        itemStyle: { color: '#f56c6c', borderRadius: [4, 4, 0, 0] },
        data: analysis.value.map(i => i.errorRate)
      }]
    })
  }
})

function studentName(studentId) {
  const student = students.value.find(s => s.id === studentId)
  return student ? student.realName : `学生#${studentId}`
}

function scoreType(row) {
  if (!row.totalScore) return 'info'
  const rate = row.score / row.totalScore
  return rate >= 0.8 ? 'success' : rate >= 0.6 ? 'warning' : 'danger'
}

async function viewDetail(row) {
  const res = await getSubmitDetail(row.id)
  detail.value = res.data
  comment.value = res.data.comment || ''
  detailVisible.value = true
}

async function handleGrade() {
  await gradeSubmit(detail.value.submitId, comment.value)
  ElMessage.success('批阅完成')
  detailVisible.value = false
  const submitRes = await getSubmits(homeworkId)
  submits.value = submitRes.data || []
}
</script>

<style scoped>
.empty-tip {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 15px;
}

.answer-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}

.answer-title {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-weight: 600;
  margin-bottom: 8px;
}

.answer-line {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.analysis {
  color: #909399;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}
</style>
