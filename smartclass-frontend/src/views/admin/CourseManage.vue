<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="课程名称搜索" clearable style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="grade" placeholder="年级" clearable style="width: 130px">
          <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">新增课程</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="name" label="课程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="学科" width="90" />
        <el-table-column prop="grade" label="年级" width="90" />
        <el-table-column prop="teacherName" label="授课教师" width="110" />
        <el-table-column prop="description" label="课程简介" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'info' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="课程名称">
          <el-input v-model="form.name" placeholder="如:初二数学培优班" />
        </el-form-item>
        <el-form-item label="学科">
          <el-select v-model="form.subjectId" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" style="width: 100%">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="form.teacherId" filterable style="width: 100%">
            <el-option v-for="t in teachers" :key="t.id" :label="t.realName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourses, saveCourse, changeCourseStatus, deleteCourse } from '../../api/teacher'
import { getSubjects, getTeachers } from '../../api/admin'

const loading = ref(false)
const records = ref([])
const subjects = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const keyword = ref('')
const grade = ref('')

const grades = ['初一', '初二', '初三', '高一', '高二', '高三']

const form = reactive({ id: null, name: '', subjectId: null, grade: '初一', teacherId: null, description: '', status: 1 })

onMounted(() => {
  loadData()
  loadOptions()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getCourses({
      keyword: keyword.value || undefined,
      grade: grade.value || undefined
    })
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [subjectRes, teacherRes] = await Promise.all([getSubjects(), getTeachers()])
  subjects.value = subjectRes.data || []
  teachers.value = teacherRes.data || []
}

function openDialog(row) {
  Object.assign(form, { id: null, name: '', subjectId: null, grade: '初一', teacherId: null, description: '', status: 1 })
  if (row) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name || !form.subjectId || !form.teacherId) {
    ElMessage.warning('请完整填写课程信息')
    return
  }
  await saveCourse({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function toggleStatus(row) {
  await changeCourseStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(row.status === 1 ? '课程已下架' : '课程已上架')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除课程「${row.name}」吗?`, '警告', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
