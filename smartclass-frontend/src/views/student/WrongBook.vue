<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>错题本</span>
          <div class="header-right">
            <el-select v-model="query.subjectId" placeholder="学科" clearable style="width: 140px" @change="loadData">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-radio-group v-model="query.mastered" @change="loadData">
              <el-radio-button :value="0">待巩固</el-radio-button>
              <el-radio-button :value="1">已掌握</el-radio-button>
              <el-radio-button :value="-1">全部</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <div v-for="item in records" :key="item.id" class="wrong-item">
          <div class="wrong-meta">
            <el-tag size="small" :type="typeTag(item.question.type)">{{ typeLabel(item.question.type) }}</el-tag>
            <el-tag v-if="item.question.subjectName" size="small" type="info" effect="plain">
              {{ item.question.subjectName }}
            </el-tag>
            <el-tag v-if="item.question.knowledgeName" size="small" effect="plain">
              {{ item.question.knowledgeName }}
            </el-tag>
            <span class="wrong-count">答错 {{ item.wrongCount }} 次 / 答对 {{ item.rightCount }} 次</span>
            <span class="wrong-time">最近做错:{{ formatTime(item.lastWrongTime) }}</span>
            <el-tag v-if="item.mastered === 1" size="small" type="success">已掌握</el-tag>
          </div>

          <div class="wrong-title">{{ item.question.title }}</div>

          <div v-if="item.question.options && item.question.options.length" class="wrong-options">
            <div v-for="(option, i) in item.question.options" :key="i" class="option-line">
              {{ String.fromCharCode(65 + i) }}. {{ option }}
            </div>
          </div>

          <div class="wrong-actions">
            <el-button size="small" type="primary" plain @click="openRedo(item)">重做本题</el-button>
            <el-button v-if="item.mastered === 0" size="small" type="success" plain @click="handleMastered(item)">
              标记已掌握
            </el-button>
            <el-button size="small" type="danger" plain @click="handleRemove(item)">移出错题本</el-button>
          </div>
        </div>

        <el-empty v-if="!records.length && !loading" description="太棒了,当前没有待巩固的错题" />
      </div>
    </el-card>

    <!-- 重做弹窗 -->
    <el-dialog v-model="redoVisible" title="错题重做" width="640px" :close-on-click-modal="false">
      <QuestionCard
        v-if="redoQuestion"
        v-model="redoAnswer"
        :question="redoQuestion"
        :index="1"
        :disabled="!!redoResult"
        :result="redoResult"
      />
      <template #footer>
        <el-button v-if="!redoResult" :disabled="!redoAnswer" type="primary" @click="handleRedoSubmit">
          提交答案
        </el-button>
        <el-button v-else type="primary" @click="redoVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuestionCard from '../../components/QuestionCard.vue'
import { getSubjects, getWrongBook, markMastered, redoWrong, removeWrong } from '../../api/student'

const loading = ref(false)
const subjects = ref([])
const records = ref([])

const query = reactive({ subjectId: null, mastered: 0 })

const redoVisible = ref(false)
const redoQuestion = ref(null)
const redoAnswer = ref('')
const redoResult = ref(null)
const redoItem = ref(null)

onMounted(async () => {
  const [subjectRes] = await Promise.all([getSubjects()])
  subjects.value = subjectRes.data || []
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      subjectId: query.subjectId || undefined,
      mastered: query.mastered === -1 ? undefined : query.mastered
    }
    const res = await getWrongBook(params)
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openRedo(item) {
  redoItem.value = item
  redoQuestion.value = item.question
  redoAnswer.value = ''
  redoResult.value = null
  redoVisible.value = true
}

async function handleRedoSubmit() {
  const res = await redoWrong({ questionId: redoQuestion.value.id, answer: redoAnswer.value, source: 'WRONG' })
  redoResult.value = res.data
  if (res.data.correct) {
    ElMessage.success('回答正确!')
  }
}

async function handleMastered(item) {
  await markMastered(item.id)
  ElMessage.success('已标记为掌握')
  await loadData()
}

async function handleRemove(item) {
  await ElMessageBox.confirm('移出后将不再跟踪该题,确定移出错题本吗?', '提示', { type: 'warning' })
  await removeWrong(item.id)
  ElMessage.success('已移出')
  await loadData()
}

function typeLabel(type) {
  return { SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', FILL: '填空题' }[type] || type
}

function typeTag(type) {
  return { SINGLE: 'primary', MULTI: 'warning', JUDGE: 'success', FILL: 'info' }[type] || 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  gap: 10px;
}

.wrong-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
}

.wrong-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.wrong-count {
  color: #f56c6c;
  font-size: 13px;
}

.wrong-time {
  color: #c0c4cc;
  font-size: 12px;
  margin-right: auto;
}

.wrong-title {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.wrong-options {
  background: #f8f9fb;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 12px;
}

.option-line {
  font-size: 13px;
  color: #606266;
  line-height: 1.9;
}

.wrong-actions {
  display: flex;
  gap: 8px;
}
</style>
