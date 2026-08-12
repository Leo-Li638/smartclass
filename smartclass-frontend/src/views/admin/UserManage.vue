<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="姓名/用户名搜索" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 130px">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
        <el-select v-model="query.clazzId" placeholder="班级" clearable style="width: 150px">
          <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <div style="flex: 1"></div>
        <el-button type="primary" plain @click="openDialog()">新增用户</el-button>
      </div>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clazzName" label="班级" width="120" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'info' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password
                    :placeholder="form.id ? '不修改请留空' : '请输入初始密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" v-if="form.role === 'STUDENT'">
          <el-select v-model="form.clazzId" clearable style="width: 100%">
            <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
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
import { getUsers, saveUser, resetPassword, changeUserStatus, deleteUser, getClasses } from '../../api/admin'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const classes = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ current: 1, size: 10, keyword: '', role: '', clazzId: null, status: null })

const form = reactive({
  id: null, username: '', password: '', realName: '', role: 'STUDENT',
  clazzId: null, phone: '', email: '', gender: '男', status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{
    validator: (rule, value, callback) => {
      if (!form.id && !value) {
        callback(new Error('请输入初始密码'))
      } else if (value && value.length < 6) {
        callback(new Error('密码至少 6 位'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

onMounted(() => {
  loadData()
  loadClasses()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers({ ...query, role: query.role || undefined, keyword: query.keyword || undefined })
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadClasses() {
  const res = await getClasses()
  classes.value = res.data || []
}

function resetQuery() {
  query.current = 1
  query.keyword = ''
  query.role = ''
  query.clazzId = null
  loadData()
}

function roleLabel(role) {
  return { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }[role] || role
}

function roleTagType(role) {
  return { ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'success' }[role] || 'info'
}

function openDialog(row) {
  Object.assign(form, {
    id: null, username: '', password: '', realName: '', role: 'STUDENT',
    clazzId: null, phone: '', email: '', gender: '男', status: 1
  })
  if (row) {
    Object.assign(form, row, { password: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  await saveUser({ ...form, password: form.password || undefined })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleResetPassword(row) {
  const { value } = await ElMessageBox.prompt(`重置 ${row.realName} 的密码为:`, '重置密码', {
    inputValue: '123456',
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度需在 6~20 位之间'
  })
  await resetPassword(row.id, value)
  ElMessage.success('密码已重置')
}

async function toggleStatus(row) {
  await changeUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(row.status === 1 ? '已停用' : '已启用')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.realName}」吗?`, '警告', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
