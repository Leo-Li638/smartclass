<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>学生注册</h2>
      <p class="tip">注册后即可登录系统进行课程学习与在线练习</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4~20 位字母或数字" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6~20 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.clazzId" placeholder="请选择班级" clearable style="width: 100%">
            <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button>
        <div class="back">
          <el-link @click="$router.push('/login')">已有账号,返回登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getClasses } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const classes = ref([])

onMounted(async () => {
  // 注册页需要班级列表,该接口无需登录鉴权数据即可拉取公开信息
  try {
    const res = await getClasses()
    classes.value = res.data || []
  } catch (e) {
    classes.value = []
  }
})

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  gender: '男',
  clazzId: null
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '4~20 位字母或数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6~20 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        value !== form.password ? callback(new Error('两次输入的密码不一致')) : callback()
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      gender: form.gender,
      clazzId: form.clazzId
    })
    ElMessage.success('注册成功,请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1c3faa 0%, #2563eb 45%, #38bdf8 100%);
}

.register-card {
  width: 460px;
  padding: 12px 8px;
}

.register-card h2 {
  text-align: center;
  margin-bottom: 8px;
}

.tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.back {
  text-align: center;
  margin-top: 14px;
}
</style>
