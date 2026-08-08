<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-left">
        <h1>智学云课堂</h1>
        <p class="slogan">K12 一站式智慧教学平台</p>
        <ul class="features">
          <li><el-icon><Check /></el-icon> 课程学习 · 在线作业 · 自动批改</li>
          <li><el-icon><Check /></el-icon> 错题本 · 薄弱知识点智能分析</li>
          <li><el-icon><Check /></el-icon> 个性化推题与学习计划生成</li>
        </ul>
      </div>
      <div class="login-right">
        <h2>欢迎登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
          <div class="extra">
            <span>还没有账号?</span>
            <el-link type="primary" @click="$router.push('/register')">学生注册</el-link>
          </div>
          <el-divider>演示账号</el-divider>
          <div class="demo-accounts">
            <el-tag @click="fill('admin', 'admin123')">管理员 admin</el-tag>
            <el-tag type="success" @click="fill('teacher1', 'teacher123')">教师 teacher1</el-tag>
            <el-tag type="warning" @click="fill('student1', 'student123')">学生 student1</el-tag>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fill(username, password) {
  form.username = username
  form.password = password
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    const home = { ADMIN: '/admin/dashboard', TEACHER: '/teacher/homeworks', STUDENT: '/student/dashboard' }
    router.push(home[res.data.role] || '/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1c3faa 0%, #2563eb 45%, #38bdf8 100%);
}

.login-panel {
  width: 880px;
  min-height: 520px;
  display: flex;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.login-left {
  flex: 1;
  background: rgba(12, 30, 80, 0.55);
  color: #fff;
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-left h1 {
  font-size: 34px;
  letter-spacing: 2px;
}

.slogan {
  margin: 14px 0 40px;
  font-size: 15px;
  opacity: 0.85;
}

.features {
  list-style: none;
}

.features li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 14px;
  opacity: 0.9;
}

.login-right {
  width: 420px;
  background: #fff;
  padding: 52px 44px;
}

.login-right h2 {
  margin-bottom: 28px;
  text-align: center;
  color: #303133;
}

.login-btn {
  width: 100%;
}

.extra {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.demo-accounts {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.demo-accounts .el-tag {
  cursor: pointer;
}
</style>
