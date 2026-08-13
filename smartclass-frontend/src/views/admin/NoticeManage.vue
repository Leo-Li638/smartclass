<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <span class="title">公告列表</span>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">发布公告</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="面向对象" width="110">
          <template #default="{ row }">
            <el-tag :type="targetType(row.targetRole)" size="small">{{ targetLabel(row.targetRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="面向对象">
          <el-select v-model="form.targetRole" style="width: 100%">
            <el-option label="所有人" value="ALL" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotices, saveNotice, deleteNotice } from '../../api/admin'

const loading = ref(false)
const records = ref([])
const dialogVisible = ref(false)

const form = reactive({ id: null, title: '', content: '', targetRole: 'ALL' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getNotices()
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

function targetLabel(role) {
  return { ALL: '所有人', TEACHER: '教师', STUDENT: '学生' }[role] || role
}

function targetType(role) {
  return { ALL: 'primary', TEACHER: 'warning', STUDENT: 'success' }[role] || 'info'
}

function openDialog(row) {
  Object.assign(form, { id: null, title: '', content: '', targetRole: 'ALL' })
  if (row) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写公告标题与内容')
    return
  }
  await saveNotice({ ...form })
  ElMessage.success('发布成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗?`, '警告', { type: 'warning' })
  await deleteNotice(row.id)
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
