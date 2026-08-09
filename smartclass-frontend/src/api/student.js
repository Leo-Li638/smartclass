import request from '../utils/request'

export const getHomeworks = params => request.get('/student/homeworks', { params })
export const getHomeworkDetail = id => request.get(`/student/homeworks/${id}`)
export const submitHomework = data => request.post('/student/homeworks/submit', data)
export const getMySubmit = homeworkId => request.get(`/student/homeworks/${homeworkId}/my-submit`)
export const getSubmitDetail = id => request.get(`/student/submits/${id}`)

export const startPractice = params => request.get('/student/practice', { params })
export const submitPractice = data => request.post('/student/practice/submit', data)

export const getWrongBook = params => request.get('/student/wrong-book', { params })
export const redoWrong = data => request.post('/student/wrong-book/redo', data)
export const markMastered = id => request.post(`/student/wrong-book/${id}/mastered`)
export const removeWrong = id => request.delete(`/student/wrong-book/${id}`)

export const getKnowledgeStats = subjectId => request.get('/student/recommend/knowledge-stats', { params: { subjectId } })
export const getRecommend = (subjectId, count) => request.get('/student/recommend', { params: { subjectId, count } })

export const generatePlan = data => request.post('/student/plans', data)
export const getPlans = () => request.get('/student/plans')
export const getPlanDetail = id => request.get(`/student/plans/${id}`)
export const deletePlan = id => request.delete(`/student/plans/${id}`)

export const getDashboard = () => request.get('/student/dashboard')
export const getSubjects = () => request.get('/student/subjects')

export const getCourses = params => request.get('/courses', { params })
export const getCourseMaterials = courseId => request.get(`/courses/${courseId}/materials`)
