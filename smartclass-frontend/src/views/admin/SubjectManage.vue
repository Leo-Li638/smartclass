<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <span class="title">学科列表</span>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">新增学科</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="name" label="学科名称" width="180" />
        <el-table-column prop="stage" label="适用学段" width="140" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学科' : '新增学科'" width="400px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学科名称">
          <el-input v-model="form.name" placeholder="如:数学" />
        </el-form-item>
        <el-form-item label="适用学段">
          <el-select v-model="form.stage" style="width: 100%">
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
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
import { getSubjects, saveSubject, deleteSubject } from '../../api/admin'

const loading = ref(false)
const records = ref([])
const dialogVisible = ref(false)

const form = reactive({ id: null, name: '', stage: '初中' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getSubjects()
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, { id: null, name: '', stage: '初中' })
  if (row) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) {
    ElMessage.warning('请填写学科名称')
    return
  }
  await saveSubject({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`删除学科「${row.name}」后,相关题目将无法按学科筛选,确定删除吗?`, '警告', { type: 'warning' })
  await deleteSubject(row.id)
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
