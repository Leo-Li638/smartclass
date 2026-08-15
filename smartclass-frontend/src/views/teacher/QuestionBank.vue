<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <el-select v-model="query.subjectId" placeholder="学科" clearable style="width: 130px" @change="loadKnowledge">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="query.knowledgeId" placeholder="知识点" clearable style="width: 160px">
          <el-option v-for="k in knowledgeList" :key="k.id" :label="k.name" :value="k.id" />
        </el-select>
        <el-select v-model="query.type" placeholder="题型" clearable style="width: 110px">
          <el-option label="单选题" value="SINGLE" />
          <el-option label="多选题" value="MULTI" />
          <el-option label="判断题" value="JUDGE" />
          <el-option label="填空题" value="FILL" />
        </el-select>
        <el-select v-model="query.difficulty" placeholder="难度" clearable style="width: 100px">
          <el-option v-for="d in 5" :key="d" :label="d + ' 星'" :value="d" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="题干关键词" clearable style="width: 180px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">新增题目</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="knowledgeName" label="知识点" width="130" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="110">
          <template #default="{ row }">
            <el-rate :model-value="row.difficulty" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column prop="answer" label="答案" width="90" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="680px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="学科" prop="subjectId">
              <el-select v-model="form.subjectId" @change="onSubjectChange">
                <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="知识点" prop="knowledgeId">
              <el-select v-model="form.knowledgeId">
                <el-option v-for="k in dialogKnowledge" :key="k.id" :label="k.name" :value="k.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" @change="onTypeChange">
                <el-option label="单选题" value="SINGLE" />
                <el-option label="多选题" value="MULTI" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="填空题" value="FILL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-rate v-model="form.difficulty" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认分值">
              <el-input-number v-model="form.score" :min="1" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题干" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" placeholder="请输入题干内容" />
        </el-form-item>
        <el-form-item label="选项" v-if="form.type === 'SINGLE' || form.type === 'MULTI'">
          <div style="width: 100%">
            <div v-for="(option, index) in form.options" :key="index" class="option-row">
              <el-input v-model="form.options[index]" :placeholder="`选项 ${String.fromCharCode(65 + index)}`" />
              <el-button link type="danger" :disabled="form.options.length <= 2" @click="form.options.splice(index, 1)">
                删除
              </el-button>
            </div>
            <el-button link type="primary" :disabled="form.options.length >= 6" @click="form.options.push('')">
              + 添加选项
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="标准答案" prop="answer">
          <el-select v-if="form.type === 'SINGLE'" v-model="form.answer" style="width: 160px">
            <el-option v-for="(o, i) in form.options" :key="i" :label="String.fromCharCode(65 + i)" :value="String.fromCharCode(65 + i)" />
          </el-select>
          <el-select v-else-if="form.type === 'MULTI'" v-model="multiAnswer" multiple style="width: 240px">
            <el-option v-for="(o, i) in form.options" :key="i" :label="String.fromCharCode(65 + i)" :value="String.fromCharCode(65 + i)" />
          </el-select>
          <el-radio-group v-else-if="form.type === 'JUDGE'" v-model="form.answer">
            <el-radio value="对">对</el-radio>
            <el-radio value="错">错</el-radio>
          </el-radio-group>
          <el-input v-else v-model="form.answer" placeholder="填空题标准答案,多个可选答案用 | 分隔" />
        </el-form-item>
        <el-form-item label="答案解析">
          <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="解题思路与知识点讲解" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestions, saveQuestion, deleteQuestion, getKnowledge, getSubjects } from '../../api/teacher'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const subjects = ref([])
const knowledgeList = ref([])
const dialogKnowledge = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ current: 1, size: 10, subjectId: null, knowledgeId: null, type: '', difficulty: null, keyword: '' })

const form = reactive({
  id: null, subjectId: null, knowledgeId: null, type: 'SINGLE',
  difficulty: 3, title: '', options: ['', '', '', ''], answer: '', analysis: '', score: 5
})

const multiAnswer = ref([])

const rules = {
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  knowledgeId: [{ required: true, message: '请选择知识点', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  title: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请设置标准答案', trigger: 'blur' }]
}

const finalAnswer = computed(() =>
  form.type === 'MULTI' ? multiAnswer.value.sort().join('') : form.answer
)

onMounted(async () => {
  await loadSubjects()
  await loadData()
})

async function loadSubjects() {
  const res = await getSubjects()
  subjects.value = res.data || []
}

async function loadKnowledge(subjectId) {
  query.knowledgeId = null
  if (!subjectId) {
    knowledgeList.value = []
    return
  }
  const res = await getKnowledge(subjectId)
  knowledgeList.value = res.data || []
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) delete params[key]
    })
    const res = await getQuestions(params)
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function typeLabel(type) {
  return { SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', FILL: '填空题' }[type] || type
}

async function onSubjectChange(subjectId) {
  form.knowledgeId = null
  if (!subjectId) {
    dialogKnowledge.value = []
    return
  }
  const res = await getKnowledge(subjectId)
  dialogKnowledge.value = res.data || []
}

function onTypeChange(type) {
  form.answer = ''
  multiAnswer.value = []
  if (type === 'JUDGE') {
    form.answer = '对'
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: null, subjectId: null, knowledgeId: null, type: 'SINGLE',
    difficulty: 3, title: '', options: ['', '', '', ''], answer: '', analysis: '', score: 5
  })
  multiAnswer.value = []
  if (row) {
    Object.assign(form, {
      id: row.id, subjectId: row.subjectId, knowledgeId: row.knowledgeId, type: row.type,
      difficulty: row.difficulty, title: row.title,
      options: (row.options && row.options.length) ? [...row.options] : ['', ''],
      answer: row.answer, analysis: row.analysis, score: row.score
    })
    if (row.type === 'MULTI') {
      multiAnswer.value = row.answer.split('')
    }
    if (row.type === 'JUDGE') {
      form.options = ['对', '错']
    }
    if (row.type === 'FILL') {
      form.options = []
    }
    onSubjectChange(row.subjectId)
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  if ((form.type === 'SINGLE' || form.type === 'MULTI')) {
    const validOptions = form.options.filter(o => o && o.trim())
    if (validOptions.length < 2) {
      ElMessage.warning('请至少填写两个选项')
      return
    }
    if (form.type === 'MULTI' && multiAnswer.value.length < 2) {
      ElMessage.warning('多选题请至少选择两个正确答案')
      return
    }
  }
  const payload = {
    ...form,
    options: (form.type === 'SINGLE' || form.type === 'MULTI')
      ? form.options.filter(o => o && o.trim()).map((o, i) => `${String.fromCharCode(65 + i)}. ${o.replace(/^[A-Z].\s*/, '')}`)
      : (form.type === 'JUDGE' ? ['对', '错'] : []),
    answer: finalAnswer.value
  }
  await saveQuestion(payload)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该题目吗?已被作业引用的题目无法删除。', '警告', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.option-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
</style>
