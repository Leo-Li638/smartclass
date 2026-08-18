<template>
  <div class="page-container">
    <!-- 练习配置 -->
    <el-card v-if="!practicing" shadow="never">
      <template #header>自主练习</template>
      <div class="setup-form">
        <el-form inline>
          <el-form-item label="学科">
            <el-select v-model="setup.subjectId" placeholder="全部学科" clearable style="width: 160px">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="setup.difficulty" placeholder="不限" clearable style="width: 120px">
              <el-option v-for="d in 5" :key="d" :label="d + ' 星'" :value="d" />
            </el-select>
          </el-form-item>
          <el-form-item label="题量">
            <el-select v-model="setup.count" style="width: 100px">
              <el-option v-for="c in [5, 10, 15, 20]" :key="c" :label="c + ' 题'" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleStart">开始练习</el-button>
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon>
          逐题作答、即时判分,答错的题目会自动进入错题本,并影响智能推题对你的掌握度评估。
        </el-alert>
      </div>
    </el-card>

    <!-- 答题中 -->
    <el-card v-else shadow="never">
      <template #header>
        <div class="card-header">
          <span>第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <div class="header-right">
            <span class="correct-stat">已答对 {{ correctCount }} 题</span>
            <el-button link type="danger" @click="handleQuit">结束练习</el-button>
          </div>
        </div>
      </template>

      <el-progress :percentage="progress" :stroke-width="8" class="progress" />

      <QuestionCard
        :key="currentQuestion.id"
        v-model="myAnswer"
        :question="currentQuestion"
        :index="currentIndex + 1"
        :disabled="!!currentResult"
        :result="currentResult"
      />

      <div class="actions">
        <template v-if="!currentResult">
          <el-button @click="handleSkip">跳过本题</el-button>
          <el-button type="primary" :disabled="!myAnswer" @click="handleSubmit">提交答案</el-button>
        </template>
        <template v-else>
          <el-button
            v-if="currentIndex < questions.length - 1"
            type="primary"
            @click="handleNext"
          >
            下一题
          </el-button>
          <el-button v-else type="success" @click="handleFinish">查看总结</el-button>
        </template>
      </div>
    </el-card>

    <!-- 练习总结 -->
    <el-dialog v-model="summaryVisible" title="练习总结" width="520px" :close-on-click-modal="false">
      <div class="summary">
        <div class="summary-score">{{ correctCount }} / {{ answeredCount }}</div>
        <div class="summary-label">
          共答 {{ answeredCount }} 题,答对 {{ correctCount }} 题
          <template v-if="answeredCount">,正确率 {{ accuracy }}%</template>
        </div>
        <div class="summary-tip">
          {{ correctCount === answeredCount && answeredCount > 0 ? '全部正确,太棒了!' : '答错的题已收录进错题本,记得去巩固。' }}
        </div>
      </div>
      <template #footer>
        <el-button @click="summaryVisible = false; handleQuit()">返回配置</el-button>
        <el-button type="primary" @click="summaryVisible = false; handleRestart()">再来一组</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import QuestionCard from '../../components/QuestionCard.vue'
import { getSubjects, startPractice, submitPractice } from '../../api/student'

const subjects = ref([])
const loading = ref(false)
const practicing = ref(false)
const questions = ref([])
const currentIndex = ref(0)
const myAnswer = ref('')
const currentResult = ref(null)
const correctCount = ref(0)
const answeredCount = ref(0)
const summaryVisible = ref(false)

const setup = reactive({ subjectId: null, difficulty: null, count: 10 })

const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
const progress = computed(() =>
  questions.value.length ? Math.round((currentIndex.value / questions.value.length) * 100) : 0
)
const accuracy = computed(() =>
  answeredCount.value ? Math.round((correctCount.value / answeredCount.value) * 100) : 0
)

onMounted(async () => {
  const res = await getSubjects()
  subjects.value = res.data || []
})

async function handleStart() {
  loading.value = true
  try {
    const res = await startPractice({
      subjectId: setup.subjectId || undefined,
      difficulty: setup.difficulty || undefined,
      count: setup.count
    })
    const list = res.data || []
    if (!list.length) {
      ElMessage.warning('没有符合条件的题目,请调整学科或难度')
      return
    }
    questions.value = list
    resetAnswerState()
    practicing.value = true
  } finally {
    loading.value = false
  }
}

function resetAnswerState() {
  currentIndex.value = 0
  correctCount.value = 0
  answeredCount.value = 0
  myAnswer.value = ''
  currentResult.value = null
}

async function handleSubmit() {
  await submitOne(myAnswer.value)
}

async function handleSkip() {
  await submitOne('')
}

async function submitOne(answer) {
  const res = await submitPractice({ questionId: currentQuestion.value.id, answer, source: 'PRACTICE' })
  currentResult.value = res.data
  answeredCount.value++
  if (res.data.correct) {
    correctCount.value++
  }
}

function handleNext() {
  currentIndex.value++
  myAnswer.value = ''
  currentResult.value = null
}

function handleFinish() {
  summaryVisible.value = true
}

function handleRestart() {
  handleStart()
}

function handleQuit() {
  practicing.value = false
  questions.value = []
  resetAnswerState()
}
</script>

<style scoped>
.setup-form {
  padding: 8px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.correct-stat {
  color: #67c23a;
  font-weight: 600;
}

.progress {
  margin-bottom: 18px;
}

.actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.summary {
  text-align: center;
  padding: 10px 0;
}

.summary-score {
  font-size: 42px;
  font-weight: 700;
  color: #409eff;
}

.summary-label {
  margin-top: 8px;
  color: #606266;
}

.summary-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}
</style>
