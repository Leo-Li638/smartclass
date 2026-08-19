<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>基本信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="账号">{{ info.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ info.realName }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag size="small">{{ roleLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="班级">{{ info.clazzName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ info.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ info.email || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>修改密码</template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 420px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="6-20 位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleChangePassword">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, getUserInfo } from '../../api/auth'

const info = ref({})
const formRef = ref()
const saving = ref(false)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleLabel = computed(() => ({
  ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生'
}[info.value.role] || info.value.role))

const genderText = computed(() => ({ 1: '男', 2: '女' }[info.value.gender] || '未设置'))

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度需在 6 到 20 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  const res = await getUserInfo()
  info.value = res.data || {}
})

async function handleChangePassword() {
  await formRef.value.validate()
  saving.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功,下次登录请使用新密码')
    Object.assign(form, { oldPassword: '', newPassword: '', confirmPassword: '' })
    formRef.value.resetFields()
  } finally {
    saving.value = false
  }
}
</script>
