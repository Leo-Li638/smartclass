<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>选择题库题目</span>
              <el-tag type="primary">已选 {{ selected.length }} 题 / 共 {{ selectedScore }} 分</el-tag>
            </div>
          </template>

          <div class="filter-bar">
            <el-select v-model="subjectId" placeholder="学科" clearable style="width: 130px" @change="loadQuestions">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-input v-model="keyword" placeholder="题干搜索" clearable style="width: 180px" @keyup.enter="loadQuestions" />
            <el-button type="primary" @click="loadQuestions">查询</el-button>
          </div>

          <el-table :data="questions" stripe v-loading="loading" max-height="460">
            <el-table-column label="" width="60">
              <template #default="{ row }">
                <el-checkbox :model-value="isSelected(row.id)" @change="toggle(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="题干" min-width="240" show-overflow-tooltip />
            <el-table-column prop="knowledgeName" label="知识点" width="120" />
            <el-table-column label="题型" width="80">
              <template #default="{ row }">{{ typeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column label="难度" width="90">
              <template #default="{ row }">{{ row.difficulty }} 星</template>
            </el-table-column>
            <el-table-column label="本题分值" width="130">
              <template #default="{ row }">
                <el-input-number v-if="isSelected(row.id)" :model-value="getSelected(row.id).score"
                                 :min="1" :max="100" size="small" @update:model-value="v => getSelected(row.id).score = v" />
                <span v-else>{{ row.score }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>作业信息</template>
          <el-form :model="form" label-width="80px">
            <el-form-item label="作业标题">
              <el-input v-model="form.title" placeholder="如:一元二次方程专项练习" />
            </el-form-item>
            <el-form-item label="所属课程">
              <el-select v-model="form.courseId" style="width: 100%">
                <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="布置班级">
              <el-select v-model="form.clazzId" style="width: 100%">
                <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
            <el-form-item label="截止时间">
              <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
            <el-form-item label="作业说明">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="作答要求与注意事项" />
            </el-form-item>

            <el-divider content-position="left">已选题目</el-divider>
            <div class="selected-list">
              <div v-if="!selected.length" class="empty-tip">请从左侧题库勾选题目</div>
              <div v-for="(item, index) in selected" :key="item.questionId" class="selected-item">
                <span>{{ index + 1 }}. {{ item.title }}</span>
                <span class="score">{{ item.score }} 分</span>
                <el-button link type="danger" size="small" @click="removeSelected(item.questionId)">移除</el-button>
              </div>
            </div>

            <div style="margin-top: 16px; display: flex; gap: 10px">
              <el-button @click="handleSave(0)">存为草稿</el-button>
              <el-button type="primary" style="flex: 1" @click="handleSave(1)">立即发布</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionList, getMyCourses, getSubjects, getClasses, saveHomework } from '../../api/teacher'

const router = useRouter()
const loading = ref(false)
const questions = ref([])
const subjects = ref([])
const courses = ref([])
const classes = ref([])
const subjectId = ref(null)
const keyword = ref('')
const selected = ref([])

const form = reactive({
  title: '', courseId: null, clazzId: null,
  startTime: new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19),
  endTime: new Date(Date.now() + 5 * 86400000 - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19),
  description: ''
})

const selectedScore = computed(() => selected.value.reduce((sum, i) => sum + i.score, 0))

onMounted(async () => {
  const [subjectRes, courseRes, classRes] = await Promise.all([getSubjects(), getMyCourses(), getClasses()])
  subjects.value = subjectRes.data || []
  courses.value = courseRes.data || []
  classes.value = classRes.data || []
  loadQuestions()
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getQuestionList(subjectId.value || undefined)
    let list = res.data || []
    if (keyword.value) {
      list = list.filter(q => q.title.includes(keyword.value))
    }
    questions.value = list
  } finally {
    loading.value = false
  }
}

function typeLabel(type) {
  return { SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', FILL: '填空题' }[type] || type
}

function isSelected(id) {
  return selected.value.some(i => i.questionId === id)
}

function getSelected(id) {
  return selected.value.find(i => i.questionId === id)
}

function toggle(question) {
  if (isSelected(question.id)) {
    removeSelected(question.id)
  } else {
    selected.value.push({
      questionId: question.id,
      score: question.score || 5,
      title: question.title
    })
  }
}

function removeSelected(id) {
  const index = selected.value.findIndex(i => i.questionId === id)
  if (index > -1) {
    selected.value.splice(index, 1)
  }
}

async function handleSave(publish) {
  if (!form.title || !form.courseId || !form.clazzId) {
    ElMessage.warning('请完整填写作业标题、课程与班级')
    return
  }
  if (!selected.value.length) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  await saveHomework({
    ...form,
    publish,
    questions: selected.value.map(i => ({ questionId: i.questionId, score: i.score }))
  })
  ElMessage.success(publish === 1 ? '作业已发布' : '草稿已保存')
  router.push('/teacher/homeworks')
}
</script>

<style scoped>
.selected-list {
  max-height: 260px;
  overflow-y: auto;
}

.selected-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
}

.selected-item span:first-child {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.score {
  color: #e6a23c;
  font-weight: 600;
}

.empty-tip {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 20px 0;
}
</style>
