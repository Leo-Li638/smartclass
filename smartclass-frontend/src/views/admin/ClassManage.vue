<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <span class="title">班级列表</span>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">新增班级</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="name" label="班级名称" width="180" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column label="班主任" width="140">
          <template #default="{ row }">{{ teacherName(row.headTeacherId) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="班级名称">
          <el-input v-model="form.name" placeholder="如:初二(3)班" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" style="width: 100%">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任">
          <el-select v-model="form.headTeacherId" clearable filterable style="width: 100%">
            <el-option v-for="t in teachers" :key="t.id" :label="t.realName" :value="t.id" />
          </el-select>
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
import { getClasses, saveClass, deleteClass, getTeachers } from '../../api/admin'

const loading = ref(false)
const records = ref([])
const teachers = ref([])
const dialogVisible = ref(false)

const grades = ['初一', '初二', '初三', '高一', '高二', '高三']

const form = reactive({ id: null, name: '', grade: '初一', headTeacherId: null })

onMounted(() => {
  loadData()
  loadTeachers()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getClasses()
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadTeachers() {
  const res = await getTeachers()
  teachers.value = res.data || []
}

function teacherName(id) {
  if (!id) return '未设置'
  const teacher = teachers.value.find(t => t.id === id)
  return teacher ? teacher.realName : '已离职教师'
}

function openDialog(row) {
  Object.assign(form, { id: null, name: '', grade: '初一', headTeacherId: null })
  if (row) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) {
    ElMessage.warning('请填写班级名称')
    return
  }
  await saveClass({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除班级「${row.name}」吗?`, '警告', { type: 'warning' })
  await deleteClass(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.title {
  font-size: 15px;
  font-weight: 600;
}
</style>
