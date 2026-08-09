import request from '../utils/request'

export const login = data => request.post('/auth/login', data)
export const register = data => request.post('/auth/register', data)
export const getClasses = () => request.get('/auth/classes')
export const getUserInfo = () => request.get('/auth/info')
export const changePassword = data => request.post('/auth/password', data)
