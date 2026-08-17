<template>
  <div class="page-container">
    <!-- 作业信息 -->
    <el-card shadow="never" class="mb16">
      <template #header>
        <div class="card-header">
          <span>{{ homework.title || '在线答题' }}</span>
          <el-button @click="$router.back()">返回列表</el-button>
        </div>
      </template>
      <el-descriptions :column="4" size="small">
        <el-descriptions-item label="课程">{{ homework.courseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="题数">{{ questions.length }} 题</el-descriptions-item>
        <el-descriptions-item label="总分">{{ totalScore }} 分</el-descriptions-item>
        <el-descriptions-item label="截止时间">
          <span :class="{ overdue: isOverdue }">{{ formatTime(homework.endTime) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="homework.description" class="homework-desc">{{ homework.description }}</div>
    </el-card>

    <!-- 已提交:成绩查看 -->
    <el-card v-if="submitted" shadow="never">
      <template #header>我的成绩</template>
      <div class="score-bar">
        <div class="score-item">
          <div class="score-num">{{ detail.score }} / {{ detail.totalScore }}</div>
          <div class="score-label">得分</div>
        </div>
        <div class="score-item">
          <div class="score-num">{{ correctCount }} / {{ detail.answers?.length || 0 }}</div>
          <div class="score-label">答对题数</div>
        </div>
        <div class="score-item">
          <div class="score-num">{{ accuracy }}%</div>
          <div class="score-label">正确率</div>
        </div>
        <div class="score-item">
          <div class="score-num">{{ statusText(detail.status) }}</div>
          <div class="score-label">批阅状态</div>
        </div>
      </div>

      <el-alert
        v-if="detail.comment"
        :title="`教师评语:${detail.comment}`"
        type="info"
        :closable="false"
        show-icon
        class="mb16"
      />

      <div v-for="(answer, i) in detail.answers" :key="answer.questionId" class="answer-item">
        <div class="answer-head">
          <span class="answer-index">{{ i + 1 }}.</span>
          <el-tag size="small" :type="typeTag(answer.type)">{{ typeLabel(answer.type) }}</el-tag>
          <el-tag size="small" :type="answer.isCorrect === 1 ? 'success' : 'danger'">
            {{ answer.isCorrect === 1 ? '答对' : '答错' }}
          </el-tag>
          <span class="answer-score">
            得分 {{ answer.score }} / {{ answer.questionScore }}
          </span>
        </div>
        <div class="answer-title">{{ answer.title }}</div>
        <div v-if="answer.options && answer.options.length" class="answer-options">
          <div v-for="(option, j) in answer.options" :key="j">
            {{ String.fromCharCode(65 + j) }}. {{ option }}
          </div>
        </div>
        <div class="answer-compare">
          <div class="compare-line">
            <span class="compare-label">我的答案:</span>
            <span :class="answer.isCorrect === 1 ? 'text-success' : 'text-danger'">
              {{ answer.myAnswer || '(未作答)' }}
            </span>
          </div>
          <div class="compare-line" v-if="answer.isCorrect !== 1">
            <span class="compare-label">正确答案:</span>
            <span class="text-success">{{ answer.rightAnswer }}</span>
          </div>
        </div>
        <div v-if="answer.analysis" class="answer-analysis">解析:{{ answer.analysis }}</div>
      </div>
    </el-card>

    <!-- 未提交:在线作答 -->
    <el-card v-else shadow="never">
      <template #header>在线作答</template>
      <div v-for="(item, i) in questions" :key="item.question.id" class="do-item">
        <QuestionCard
          v-model="answers[item.question.id]"
          :question="item.question"
          :index="i + 1"
          :score-text="`本题 ${item.score} 分`"
        />
      </div>
      <div class="submit-bar">
        <span class="answered-count">已作答 {{ answeredCount }} / {{ questions.length }} 题</span>
        <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
          交卷
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuestionCard from '../../components/QuestionCard.vue'
import { getHomeworkDetail, getSubmitDetail, submitHomework } from '../../api/student'

const route = useRoute()
const router = useRouter()

const homework = ref({})
const questions = ref([])
const submitRecord = ref(null)
const detail = ref({})
const submitted = computed(() => !!submitRecord.value)
const submitting = ref(false)

const answers = reactive({})

const totalScore = computed(() => questions.value.reduce((sum, item) => sum + (item.score || 0), 0))
const answeredCount = computed(() => questions.value.filter(item => answers[item.question.id]).length)
const correctCount = computed(() => (detail.value.answers || []).filter(a => a.isCorrect === 1).length)
const accuracy = computed(() => {
  const list = detail.value.answers || []
  return list.length ? Math.round((correctCount.value / list.length) * 100) : 0
})
const isOverdue = computed(() =>
  homework.value.endTime && new Date(homework.value.endTime).getTime() < Date.now()
)

onMounted(async () => {
  const res = await getHomeworkDetail(route.params.id)
  homework.value = res.data.homework || {}
  questions.value = res.data.questions || []
  submitRecord.value = res.data.submit || null
  if (submitRecord.value) {
    const detailRes = await getSubmitDetail(submitRecord.value.id)
    detail.value = detailRes.data || {}
  }
})

async function handleSubmit() {
  const unanswered = questions.value.length - answeredCount.value
  const message = unanswered > 0
    ? `还有 ${unanswered} 题未作答,未作答的题记 0 分,确定交卷吗?`
    : '交卷后将立即自动判分,且不能修改答案,确定交卷吗?'
  const confirmed = await ElMessageBox.confirm(message, '提示', { type: 'warning' })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  submitting.value = true
  try {
    const res = await submitHomework({
      homeworkId: Number(route.params.id),
      answers: questions.value.map(item => ({
        questionId: item.question.id,
        answer: answers[item.question.id] || ''
      }))
    })
    ElMessage.success(`交卷成功!得分 ${res.data.score} / ${res.data.totalScore}`)
    const again = await getHomeworkDetail(route.params.id)
    homework.value = again.data.homework || homework.value
    submitRecord.value = again.data.submit || null
    if (submitRecord.value) {
      const d = await getSubmitDetail(submitRecord.value.id)
      detail.value = d.data || {}
    }
  } finally {
    submitting.value = false
  }
}

function typeLabel(type) {
  return { SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', FILL: '填空题' }[type] || type
}

function typeTag(type) {
  return { SINGLE: 'primary', MULTI: 'warning', JUDGE: 'success', FILL: 'info' }[type] || 'info'
}

function statusText(status) {
  return { 1: '待批阅', 2: '已批阅' }[status] || '未知'
}

function formatTime(time) {
  if (!time) return '不限时'
  return String(time).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.homework-desc {
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
}

.overdue {
  color: #f56c6c;
}

.score-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.score-item {
  flex: 1;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  padding: 18px 0;
}

.score-num {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}

.score-label {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.answer-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.answer-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.answer-index {
  font-weight: 600;
}

.answer-score {
  margin-left: auto;
  color: #909399;
  font-size: 13px;
}

.answer-title {
  font-size: 15px;
  line-height: 1.8;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.answer-options {
  background: #f8f9fb;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
  line-height: 1.9;
}

.answer-compare {
  font-size: 14px;
}

.compare-line {
  margin-bottom: 4px;
}

.compare-label {
  color: #909399;
  margin-right: 8px;
}

.answer-analysis {
  margin-top: 10px;
  background: #f0f9eb;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
}

.do-item {
  margin-bottom: 14px;
}

.submit-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 8px;
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 12px 0;
  border-top: 1px solid #ebeef5;
}

.answered-count {
  color: #909399;
  font-size: 13px;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
