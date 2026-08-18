<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>课程学习</template>

      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="课程名称"
          clearable
          style="width: 200px"
          @keyup.enter="loadData"
        />
        <el-select v-model="grade" placeholder="年级" clearable style="width: 130px" @change="loadData">
          <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col :span="8" v-for="course in courses" :key="course.id">
          <el-card shadow="hover" class="course-card">
            <div class="course-name">{{ course.name }}</div>
            <div class="course-meta">
              <el-tag size="small">{{ course.subjectName || '未设置学科' }}</el-tag>
              <el-tag type="success" size="small">{{ course.grade }}</el-tag>
            </div>
            <p class="course-desc">{{ course.description || '暂无课程简介' }}</p>
            <div class="course-footer">
              <span class="course-teacher">授课教师:{{ course.teacherName || '-' }}</span>
              <el-button size="small" type="primary" plain @click="openMaterials(course)">查看资料</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="24" v-if="!courses.length && !loading">
          <el-empty description="暂无上架课程" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 资料查看 -->
    <el-dialog v-model="materialVisible" :title="`学习资料 - ${currentCourse?.name || ''}`" width="680px">
      <div v-loading="materialLoading">
        <el-collapse v-if="materials.length" v-model="activeMaterial">
          <el-collapse-item v-for="material in materials" :key="material.id" :name="material.id">
            <template #title>
              <div class="material-title">
                <el-tag size="small" :type="typeTag(material.type)">{{ typeLabel(material.type) }}</el-tag>
                <span>{{ material.title }}</span>
              </div>
            </template>
            <div v-if="material.type === 'VIDEO' || material.type === 'LINK'" class="material-link">
              <el-link type="primary" :href="material.content" target="_blank">{{ material.content }}</el-link>
            </div>
            <div v-else class="material-content">{{ material.content }}</div>
          </el-collapse-item>
        </el-collapse>
        <el-empty v-else description="该课程暂无学习资料" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getCourses, getCourseMaterials } from '../../api/student'

const loading = ref(false)
const courses = ref([])
const keyword = ref('')
const grade = ref('')
const grades = ['初一', '初二', '初三', '高一', '高二', '高三']

const materialVisible = ref(false)
const materialLoading = ref(false)
const currentCourse = ref(null)
const materials = ref([])
const activeMaterial = ref([])

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getCourses({
      keyword: keyword.value || undefined,
      grade: grade.value || undefined
    })
    courses.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function openMaterials(course) {
  currentCourse.value = course
  materialVisible.value = true
  materialLoading.value = true
  try {
    const res = await getCourseMaterials(course.id)
    materials.value = res.data || []
    activeMaterial.value = []
  } finally {
    materialLoading.value = false
  }
}

function typeLabel(type) {
  return { DOC: '文档讲义', VIDEO: '视频', LINK: '外链' }[type] || type
}

function typeTag(type) {
  return { DOC: 'primary', VIDEO: 'success', LINK: 'warning' }[type] || 'info'
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.course-card {
  margin-bottom: 16px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}

.course-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.course-desc {
  font-size: 13px;
  color: #909399;
  min-height: 60px;
  line-height: 1.6;
}

.course-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.course-teacher {
  font-size: 12px;
  color: #c0c4cc;
}

.material-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.material-link {
  padding: 8px 0;
}

.material-content {
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  padding: 4px 0;
}
</style>
