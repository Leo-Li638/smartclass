<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <span class="title">我的作业</span>
        <div style="flex: 1"></div>
        <el-button type="primary" @click="$router.push('/teacher/homeworks/create')">创建作业</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="title" label="作业标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="170" show-overflow-tooltip />
        <el-table-column prop="clazzName" label="布置班级" width="110" />
        <el-table-column prop="questionCount" label="题目数" width="85" />
        <el-table-column prop="totalScore" label="总分" width="75" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交/批阅" width="100">
          <template #default="{ row }">{{ row.submitCount }} 份</template>
        </el-table-column>
        <el-table-column prop="avgScore" label="平均分" width="85" />
        <el-table-column prop="endTime" label="截止时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" link type="primary" size="small" @click="$router.push(`/teacher/grade/${row.id}`)">批阅</el-button>
            <el-button link type="warning" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHomeworks, publishHomework, deleteHomework } from '../../api/teacher'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getHomeworks(query)
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handlePublish(row) {
  await ElMessageBox.confirm(`确定向「${row.clazzName}」发布作业「${row.title}」吗?发布后学生即可作答。`, '发布作业', { type: 'info' })
  await publishHomework(row.id)
  ElMessage.success('作业已发布')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('删除作业将同时删除学生的提交记录,确定删除吗?', '警告', { type: 'warning' })
  await deleteHomework(row.id)
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
