import request from '../utils/request'

export const getDashboard = () => request.get('/admin/dashboard')

export const getUsers = params => request.get('/admin/users', { params })
export const saveUser = data => request.post('/admin/users', data)
export const resetPassword = (id, password) => request.post(`/admin/users/${id}/reset-password`, { password })
export const changeUserStatus = (id, status) => request.post(`/admin/users/${id}/status/${status}`)
export const deleteUser = id => request.delete(`/admin/users/${id}`)
export const getTeachers = () => request.get('/admin/teachers')

export const getClasses = () => request.get('/admin/classes')
export const saveClass = data => request.post('/admin/classes', data)
export const deleteClass = id => request.delete(`/admin/classes/${id}`)

export const getSubjects = () => request.get('/admin/subjects')
export const saveSubject = data => request.post('/admin/subjects', data)
export const deleteSubject = id => request.delete(`/admin/subjects/${id}`)

export const getNotices = () => request.get('/admin/notices')
export const saveNotice = data => request.post('/admin/notices', data)
export const deleteNotice = id => request.delete(`/admin/notices/${id}`)

export const getNoticesForMe = () => request.get('/common/notices')
