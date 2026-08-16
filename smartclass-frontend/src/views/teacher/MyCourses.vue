<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>我的课程</template>
      <el-row :gutter="16">
        <el-col :span="8" v-for="course in courses" :key="course.id">
          <el-card shadow="hover" class="course-card">
            <div class="course-name">{{ course.name }}</div>
            <div class="course-meta">
              <el-tag size="small">{{ course.subjectName || '未设置学科' }}</el-tag>
              <el-tag type="success" size="small">{{ course.grade }}</el-tag>
              <el-tag type="info" size="small">{{ course.status === 1 ? '上架中' : '已下架' }}</el-tag>
            </div>
            <p class="course-desc">{{ course.description || '暂无课程简介' }}</p>
            <div class="course-actions">
              <el-button size="small" @click="openMaterialDialog(course)">管理资料</el-button>
              <el-button size="small" type="primary" plain @click="openCourseDialog(course)">编辑</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8" v-if="!courses.length && !loading">
          <el-empty description="还没有课程,点击下方按钮创建" />
        </el-col>
      </el-row>
      <div style="margin-top: 16px">
        <el-button type="primary" @click="openCourseDialog()">新增课程</el-button>
      </div>
    </el-card>

    <el-dialog v-model="courseVisible" :title="courseForm.id ? '编辑课程' : '新增课程'" width="520px">
      <el-form :model="courseForm" label-width="90px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        <el-form-item label="学科">
          <el-select v-model="courseForm.subjectId" style="width: 100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="courseForm.grade" style="width: 100%">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCourse">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="materialVisible" :title="`资料管理 - ${currentCourse?.name || ''}`" width="640px">
      <el-button type="primary" plain size="small" style="margin-bottom: 12px" @click="openMaterialEdit()">新增资料</el-button>
      <el-table :data="materials" stripe size="small">
        <el-table-column prop="title" label="资料名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openMaterialEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteMaterial(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="materialEditVisible" :title="materialForm.id ? '编辑资料' : '新增资料'" width="480px" append-to-body>
        <el-form :model="materialForm" label-width="80px">
          <el-form-item label="资料名称">
            <el-input v-model="materialForm.title" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="materialForm.type" style="width: 100%">
              <el-option label="文档讲义" value="DOC" />
              <el-option label="视频" value="VIDEO" />
              <el-option label="外链" value="LINK" />
            </el-select>
          </el-form-item>
          <el-form-item :label="materialForm.type === 'VIDEO' || materialForm.type === 'LINK' ? '链接地址' : '内容'">
            <el-input v-if="materialForm.type === 'VIDEO' || materialForm.type === 'LINK'"
                      v-model="materialForm.content" placeholder="https://..." />
            <el-input v-else v-model="materialForm.content" type="textarea" :rows="5" placeholder="讲义内容" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="materialForm.sort" :min="1" :max="99" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="materialEditVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveMaterial">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCourses, saveCourse, getMaterials, saveMaterial, deleteMaterial, getSubjects } from '../../api/teacher'

const loading = ref(false)
const courses = ref([])
const subjects = ref([])
const courseVisible = ref(false)
const materialVisible = ref(false)
const materialEditVisible = ref(false)
const currentCourse = ref(null)
const materials = ref([])

const grades = ['初一', '初二', '初三', '高一', '高二', '高三']

const courseForm = reactive({ id: null, name: '', subjectId: null, grade: '初一', description: '', status: 1 })
const materialForm = reactive({ id: null, courseId: null, title: '', type: 'DOC', content: '', sort: 1 })

onMounted(async () => {
  loading.value = true
  try {
    const [courseRes, subjectRes] = await Promise.all([getMyCourses(), getSubjects()])
    courses.value = courseRes.data || []
    subjects.value = subjectRes.data || []
  } finally {
    loading.value = false
  }
})

function openCourseDialog(course) {
  Object.assign(courseForm, { id: null, name: '', subjectId: null, grade: '初一', description: '', status: 1 })
  if (course) {
    Object.assign(courseForm, course)
  }
  courseVisible.value = true
}

async function handleSaveCourse() {
  if (!courseForm.name || !courseForm.subjectId) {
    ElMessage.warning('请填写课程名称并选择学科')
    return
  }
  await saveCourse({ ...courseForm })
  ElMessage.success('保存成功')
  courseVisible.value = false
  const res = await getMyCourses()
  courses.value = res.data || []
}

async function openMaterialDialog(course) {
  currentCourse.value = course
  const res = await getMaterials(course.id)
  materials.value = res.data || []
  materialVisible.value = true
}

function openMaterialEdit(material) {
  Object.assign(materialForm, { id: null, courseId: currentCourse.value.id, title: '', type: 'DOC', content: '', sort: 1 })
  if (material) {
    Object.assign(materialForm, material)
  }
  materialEditVisible.value = true
}

async function handleSaveMaterial() {
  if (!materialForm.title) {
    ElMessage.warning('请填写资料名称')
    return
  }
  await saveMaterial({ ...materialForm })
  ElMessage.success('保存成功')
  materialEditVisible.value = false
  const res = await getMaterials(currentCourse.value.id)
  materials.value = res.data || []
}

async function handleDeleteMaterial(material) {
  await ElMessageBox.confirm(`确定删除资料「${material.title}」吗?`, '警告', { type: 'warning' })
  await deleteMaterial(material.id)
  ElMessage.success('删除成功')
  const res = await getMaterials(currentCourse.value.id)
  materials.value = res.data || []
}

function typeLabel(type) {
  return { DOC: '文档', VIDEO: '视频', LINK: '外链' }[type] || type
}

function typeTag(type) {
  return { DOC: 'primary', VIDEO: 'success', LINK: 'warning' }[type] || 'info'
}
</script>

<style scoped>
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

.course-actions {
  display: flex;
  gap: 8px;
}
</style>
