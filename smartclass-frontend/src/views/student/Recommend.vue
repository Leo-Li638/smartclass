<template>
  <div class="page-container">
    <!-- 掌握度分析 -->
    <el-card shadow="never" class="mb16">
      <template #header>
        <div class="card-header">
          <span>知识点掌握度分析</span>
          <div class="header-right">
            <el-select v-model="subjectId" placeholder="选择学科" clearable style="width: 150px" @change="loadStats">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-button type="primary" :loading="recommending" @click="handleRecommend">
              <el-icon style="margin-right: 4px"><MagicStick /></el-icon>一键智能推题
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="stats" stripe v-loading="loading" size="default">
        <el-table-column prop="knowledgeName" label="知识点" min-width="150" />
        <el-table-column prop="total" label="做题数" width="90" align="center" />
        <el-table-column label="正确率" width="170">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round(row.accuracy || 0)"
              :color="accuracyColor((row.accuracy || 0) / 100)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column label="掌握度" width="170">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round((row.mastery || 0) * 100)"
              :color="accuracyColor(row.mastery)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column label="评估" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="levelTag(row)">{{ levelText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastPracticeTime" label="最近练习" width="170">
          <template #default="{ row }">{{ formatTime(row.lastPracticeTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!stats.length && !loading" description="暂无练习数据,先去做两组题吧" />
    </el-card>

    <!-- 推荐结果 -->
    <el-card v-if="groups.length" shadow="never">
      <template #header>
        <span>为你推荐(按薄弱程度排序)</span>
      </template>
      <div v-for="group in groups" :key="group.knowledgeId" class="recommend-group">
        <div class="group-head">
          <el-icon color="#f56c6c"><Warning /></el-icon>
          <span class="group-name">{{ group.knowledgeName }}</span>
          <el-tag size="small" type="danger" effect="plain">薄弱度 {{ Math.round(group.weakScore * 100) }}%</el-tag>
          <span class="group-reason">{{ group.reason }}</span>
        </div>
        <div class="group-questions">
          <div v-for="(item, i) in group.questions" :key="item.id" class="question-item">
            <span class="question-index">{{ i + 1 }}. {{ item.title }}</span>
            <div class="question-meta">
              <el-tag size="small" effect="plain">{{ typeLabel(item.type) }}</el-tag>
              <el-tag size="small" type="info" effect="plain">难度 {{ item.difficulty }} 星</el-tag>
            </div>
            <el-button size="small" type="primary" plain @click="openPractice(item)">练习本题</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 单题练习弹窗 -->
    <el-dialog v-model="practiceVisible" title="推题练习" width="640px" :close-on-click-modal="false">
      <QuestionCard
        v-if="practiceQuestion"
        v-model="practiceAnswer"
        :question="practiceQuestion"
        :index="1"
        :disabled="!!practiceResult"
        :result="practiceResult"
      />
      <template #footer>
        <el-button v-if="!practiceResult" :disabled="!practiceAnswer" type="primary" @click="handlePracticeSubmit">
          提交答案
        </el-button>
        <el-button v-else type="primary" @click="practiceVisible = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Warning } from '@element-plus/icons-vue'
import QuestionCard from '../../components/QuestionCard.vue'
import { getKnowledgeStats, getRecommend, getSubjects, submitPractice } from '../../api/student'

const subjects = ref([])
const subjectId = ref(null)
const stats = ref([])
const groups = ref([])
const loading = ref(false)
const recommending = ref(false)

const practiceVisible = ref(false)
const practiceQuestion = ref(null)
const practiceAnswer = ref('')
const practiceResult = ref(null)

onMounted(async () => {
  const res = await getSubjects()
  subjects.value = res.data || []
  await loadStats()
})

async function loadStats() {
  loading.value = true
  try {
    const res = await getKnowledgeStats(subjectId.value || undefined)
    stats.value = res.data || []
    groups.value = []
  } finally {
    loading.value = false
  }
}

async function handleRecommend() {
  recommending.value = true
  try {
    const res = await getRecommend(subjectId.value || undefined, 3)
    groups.value = res.data || []
    if (!groups.value.length) {
      ElMessage.info('暂无可推荐的薄弱知识点,继续保持!')
    } else {
      ElMessage.success('已根据薄弱知识点生成推荐')
    }
  } finally {
    recommending.value = false
  }
}

function openPractice(question) {
  practiceQuestion.value = question
  practiceAnswer.value = ''
  practiceResult.value = null
  practiceVisible.value = true
}

async function handlePracticeSubmit() {
  const res = await submitPractice({ questionId: practiceQuestion.value.id, answer: practiceAnswer.value, source: 'PRACTICE' })
  practiceResult.value = res.data
}

function accuracyColor(value) {
  const v = (value || 0) * 100
  if (v >= 80) return '#67c23a'
  if (v >= 60) return '#e6a23c'
  return '#f56c6c'
}

function levelText(row) {
  const mastery = row.mastery || 0
  if (row.total === 0) return '未练习'
  if (mastery >= 0.8) return '掌握良好'
  if (mastery >= 0.6) return '基本掌握'
  return '薄弱'
}

function levelTag(row) {
  const mastery = row.mastery || 0
  if (row.total === 0) return 'info'
  if (mastery >= 0.8) return 'success'
  if (mastery >= 0.6) return 'warning'
  return 'danger'
}

function typeLabel(type) {
  return { SINGLE: '单选', MULTI: '多选', JUDGE: '判断', FILL: '填空' }[type] || type
}

function formatTime(time) {
  if (!time) return '未练习'
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

.header-right {
  display: flex;
  gap: 10px;
}

.recommend-group {
  border: 1px solid #f3d19e;
  background: #fdf6ec;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.group-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.group-name {
  font-weight: 600;
  font-size: 15px;
}

.group-reason {
  color: #909399;
  font-size: 13px;
}

.group-questions {
  background: #fff;
  border-radius: 6px;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px dashed #ebeef5;
}

.question-item:last-child {
  border-bottom: none;
}

.question-index {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
}

.question-meta {
  display: flex;
  gap: 6px;
}
</style>
