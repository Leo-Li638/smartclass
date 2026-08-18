<template>
  <div class="page-container">
    <el-card shadow="never" class="mb16">
      <template #header>
        <div class="card-header">
          <span>我的学习计划</span>
          <el-button type="primary" @click="openGenerate">
            <el-icon style="margin-right: 4px"><Calendar /></el-icon>AI 生成学习计划
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <div v-for="plan in plans" :key="plan.id" class="plan-card">
          <div class="plan-head">
            <div class="plan-title">{{ plan.title }}</div>
            <el-tag :type="plan.status === 1 ? 'success' : 'primary'" size="small">
              {{ plan.status === 1 ? '已完成' : '进行中' }}
            </el-tag>
          </div>
          <div class="plan-goal">{{ plan.goal }}</div>
          <div class="plan-meta">
            <span>{{ plan.subjectName }}</span>
            <el-divider direction="vertical" />
            <span>{{ plan.startDate }} ~ {{ plan.endDate }}</span>
            <el-divider direction="vertical" />
            <span>每日 {{ plan.dailyCount }} 题</span>
            <el-divider direction="vertical" />
            <span>已完成 {{ plan.doneQuestions }} / {{ plan.totalQuestions }} 题</span>
          </div>
          <el-progress
            :percentage="plan.progress || 0"
            :status="plan.progress >= 100 ? 'success' : undefined"
            :stroke-width="10"
          />
          <div class="plan-actions">
            <el-button size="small" type="primary" plain @click="openDetail(plan)">查看任务明细</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(plan)">删除计划</el-button>
          </div>
        </div>
        <el-empty v-if="!plans.length && !loading" description="还没有学习计划,点击右上角按钮生成一个吧" />
      </div>
    </el-card>

    <!-- 生成计划 -->
    <el-dialog v-model="generateVisible" title="生成学习计划" width="520px" :close-on-click-modal="false">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="mb16"
        title="系统会分析你的错题与正确率,找出薄弱知识点并按薄弱程度分配每日题量"
      />
      <el-form :model="form" label-width="100px">
        <el-form-item label="学科" required>
          <el-select v-model="form.subjectId" placeholder="选择学科" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划天数">
          <el-slider v-model="form.days" :min="3" :max="30" show-input :show-input-controls="false" />
        </el-form-item>
        <el-form-item label="每日题量">
          <el-slider v-model="form.dailyCount" :min="4" :max="30" show-input :show-input-controls="false" />
        </el-form-item>
        <el-form-item label="学习目标">
          <el-input v-model="form.goal" type="textarea" :rows="2" placeholder="选填,如:期中考试前把薄弱知识点全部消灭" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">生成计划</el-button>
      </template>
    </el-dialog>

    <!-- 计划明细 -->
    <el-drawer v-model="detailVisible" :title="detail?.title || '计划明细'" size="560px">
      <template v-if="detail">
        <div class="detail-goal">{{ detail.goal }}</div>
        <div v-for="(tasks, date) in groupedTasks" :key="date" class="day-group">
          <div class="day-title">
            <el-icon><Calendar /></el-icon>
            <span>{{ date }}</span>
            <el-tag v-if="isToday(date)" size="small" type="primary">今天</el-tag>
          </div>
          <div v-for="task in tasks" :key="task.id" class="task-item">
            <div class="task-content">
              <span>{{ task.content }}</span>
              <el-tag size="small" :type="taskStatusTag(task.status)">
                {{ taskStatusText(task.status) }}
              </el-tag>
            </div>
            <div class="task-progress">
              <el-progress
                :percentage="task.questionCount ? Math.min(100, Math.round(task.doneCount * 100 / task.questionCount)) : 0"
                :stroke-width="8"
                :color="task.status === 2 ? '#67c23a' : '#409eff'"
              />
            </div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { deletePlan, generatePlan, getPlanDetail, getPlans, getSubjects } from '../../api/student'

const loading = ref(false)
const generating = ref(false)
const plans = ref([])
const subjects = ref([])

const generateVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

const form = reactive({ subjectId: null, days: 7, dailyCount: 10, goal: '' })

const groupedTasks = computed(() => {
  const groups = {}
  for (const task of detail.value?.tasks || []) {
    const date = String(task.taskDate).slice(0, 10)
    if (!groups[date]) groups[date] = []
    groups[date].push(task)
  }
  return groups
})

onMounted(async () => {
  const subjectRes = await getSubjects()
  subjects.value = subjectRes.data || []
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getPlans()
    plans.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openGenerate() {
  Object.assign(form, { subjectId: null, days: 7, dailyCount: 10, goal: '' })
  generateVisible.value = true
}

async function handleGenerate() {
  if (!form.subjectId) {
    ElMessage.warning('请选择学科')
    return
  }
  generating.value = true
  try {
    await generatePlan({ ...form })
    ElMessage.success('学习计划已生成,记得每天坚持完成')
    generateVisible.value = false
    await loadData()
  } finally {
    generating.value = false
  }
}

async function openDetail(plan) {
  const res = await getPlanDetail(plan.id)
  detail.value = res.data
  detailVisible.value = true
}

async function handleDelete(plan) {
  await ElMessageBox.confirm(`确定删除计划「${plan.title}」吗?`, '警告', { type: 'warning' })
  await deletePlan(plan.id)
  ElMessage.success('已删除')
  await loadData()
}

function isToday(date) {
  const today = new Date()
  const mm = String(today.getMonth() + 1).padStart(2, '0')
  const dd = String(today.getDate()).padStart(2, '0')
  return date === `${today.getFullYear()}-${mm}-${dd}`
}

function taskStatusText(status) {
  return { 0: '未开始', 1: '进行中', 2: '已完成' }[status] || '未知'
}

function taskStatusTag(status) {
  return { 0: 'info', 1: 'primary', 2: 'success' }[status] || 'info'
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

.plan-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 14px;
}

.plan-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
}

.plan-goal {
  color: #606266;
  font-size: 13px;
  margin: 8px 0 10px;
}

.plan-meta {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.plan-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.detail-goal {
  color: #606266;
  font-size: 13px;
  margin-bottom: 16px;
}

.day-group {
  margin-bottom: 18px;
}

.day-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.task-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.task-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 6px;
}
</style>
