import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('../layout/Layout.vue'),
    redirect: '/admin/dashboard',
    meta: { roles: ['ADMIN'] },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '数据看板', icon: 'Odometer' } },
      { path: 'users', name: 'UserManage', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'classes', name: 'ClassManage', component: () => import('../views/admin/ClassManage.vue'), meta: { title: '班级管理', icon: 'School' } },
      { path: 'subjects', name: 'SubjectManage', component: () => import('../views/admin/SubjectManage.vue'), meta: { title: '学科管理', icon: 'Collection' } },
      { path: 'courses', name: 'CourseManage', component: () => import('../views/admin/CourseManage.vue'), meta: { title: '课程管理', icon: 'Reading' } },
      { path: 'notices', name: 'NoticeManage', component: () => import('../views/admin/NoticeManage.vue'), meta: { title: '公告管理', icon: 'Bell' } }
    ]
  },
  {
    path: '/teacher',
    component: () => import('../layout/Layout.vue'),
    redirect: '/teacher/homeworks',
    meta: { roles: ['TEACHER'] },
    children: [
      { path: 'homeworks', name: 'HomeworkManage', component: () => import('../views/teacher/HomeworkManage.vue'), meta: { title: '作业管理', icon: 'EditPen' } },
      { path: 'homeworks/create', name: 'HomeworkCreate', component: () => import('../views/teacher/HomeworkCreate.vue'), meta: { title: '创建作业', icon: 'DocumentAdd', hidden: true } },
      { path: 'questions', name: 'QuestionBank', component: () => import('../views/teacher/QuestionBank.vue'), meta: { title: '题库管理', icon: 'Notebook' } },
      { path: 'courses', name: 'TeacherCourses', component: () => import('../views/teacher/MyCourses.vue'), meta: { title: '我的课程', icon: 'Reading' } },
      { path: 'grade/:homeworkId', name: 'HomeworkGrade', component: () => import('../views/teacher/HomeworkGrade.vue'), meta: { title: '作业批阅', icon: 'Finished', hidden: true } },
      { path: 'analysis', name: 'Analysis', component: () => import('../views/teacher/Analysis.vue'), meta: { title: '学情分析', icon: 'TrendCharts' } }
    ]
  },
  {
    path: '/student',
    component: () => import('../layout/Layout.vue'),
    redirect: '/student/dashboard',
    meta: { roles: ['STUDENT'] },
    children: [
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('../views/student/Dashboard.vue'), meta: { title: '学习看板', icon: 'Odometer' } },
      { path: 'recommend', name: 'Recommend', component: () => import('../views/student/Recommend.vue'), meta: { title: '智能推题', icon: 'MagicStick' } },
      { path: 'practice', name: 'Practice', component: () => import('../views/student/Practice.vue'), meta: { title: '自主练习', icon: 'Edit' } },
      { path: 'wrong-book', name: 'WrongBook', component: () => import('../views/student/WrongBook.vue'), meta: { title: '错题本', icon: 'Tickets' } },
      { path: 'plans', name: 'MyPlans', component: () => import('../views/student/MyPlans.vue'), meta: { title: '学习计划', icon: 'Calendar' } },
      { path: 'homeworks', name: 'StudentHomeworks', component: () => import('../views/student/HomeworkList.vue'), meta: { title: '作业中心', icon: 'EditPen' } },
      { path: 'homeworks/:id', name: 'HomeworkDo', component: () => import('../views/student/HomeworkDo.vue'), meta: { title: '在线答题', hidden: true } },
      { path: 'courses', name: 'StudentCourses', component: () => import('../views/student/CourseList.vue'), meta: { title: '课程学习', icon: 'Reading' } },
      { path: 'notices', name: 'StudentNotices', component: () => import('../views/common/NoticeList.vue'), meta: { title: '系统公告', icon: 'Bell' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/common/Profile.vue'), meta: { title: '个人中心', icon: 'UserFilled', hidden: true } }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }
  if (!token || !userInfo) {
    next('/login')
    return
  }
  // 按角色隔离:访问非本角色模块直接跳回自己的首页
  const role = userInfo.role
  const roleHome = { ADMIN: '/admin/dashboard', TEACHER: '/teacher/homeworks', STUDENT: '/student/dashboard' }
  if (to.path.startsWith('/admin') && role !== 'ADMIN') {
    next(roleHome[role] || '/login')
    return
  }
  if (to.path.startsWith('/teacher') && role !== 'TEACHER') {
    next(roleHome[role] || '/login')
    return
  }
  if (to.path.startsWith('/student') && role !== 'STUDENT') {
    next(roleHome[role] || '/login')
    return
  }
  if (to.path === '/') {
    next(roleHome[role] || '/login')
    return
  }
  next()
})

export default router
