package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.BusinessException;
import com.smartclass.common.Constants;
import com.smartclass.entity.Course;
import com.smartclass.entity.CourseMaterial;
import com.smartclass.entity.Subject;
import com.smartclass.entity.User;
import com.smartclass.mapper.CourseMapper;
import com.smartclass.mapper.CourseMaterialMapper;
import com.smartclass.mapper.SubjectMapper;
import com.smartclass.mapper.UserMapper;
import com.smartclass.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程与学习资料服务
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseMaterialMapper materialMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;

    public List<Map<String, Object>> list(String keyword, String grade) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .like(StringUtils.hasText(keyword), Course::getName, keyword)
                .eq(StringUtils.hasText(grade), Course::getGrade, grade)
                .orderByDesc(Course::getCreateTime);
        if (Constants.ROLE_STUDENT.equals(UserContext.getRole())) {
            wrapper.eq(Course::getStatus, 1);
        }

        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, String> teacherNames = teacherNames();

        return courseMapper.selectList(wrapper).stream().map(course -> {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", course.getId());
            item.put("name", course.getName());
            item.put("subjectId", course.getSubjectId());
            item.put("subjectName", subjectNames.get(course.getSubjectId()));
            item.put("grade", course.getGrade());
            item.put("teacherId", course.getTeacherId());
            item.put("teacherName", teacherNames.get(course.getTeacherId()));
            item.put("description", course.getDescription());
            item.put("status", course.getStatus());
            item.put("createTime", course.getCreateTime());
            return item;
        }).collect(Collectors.toList());
    }

    public List<Course> myCourses() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, UserContext.getUserId())
                .orderByDesc(Course::getCreateTime));
    }

    public void save(Course course) {
        // 教师只能创建自己的课程;管理员可以指定授课教师
        if (Constants.ROLE_TEACHER.equals(UserContext.getRole())) {
            course.setTeacherId(UserContext.getUserId());
        }
        if (course.getId() == null) {
            courseMapper.insert(course);
        } else {
            courseMapper.updateById(course);
        }
    }

    public void changeStatus(Long id, Integer status) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        courseMapper.updateById(course);
    }

    public void delete(Long id) {
        Long materialCount = materialMapper.selectCount(new LambdaQueryWrapper<CourseMaterial>()
                .eq(CourseMaterial::getCourseId, id));
        if (materialCount > 0) {
            throw new BusinessException("请先删除该课程下的学习资料");
        }
        courseMapper.deleteById(id);
    }

    public List<CourseMaterial> materials(Long courseId) {
        return materialMapper.selectList(new LambdaQueryWrapper<CourseMaterial>()
                .eq(CourseMaterial::getCourseId, courseId)
                .orderByAsc(CourseMaterial::getSort)
                .orderByAsc(CourseMaterial::getId));
    }

    public void saveMaterial(CourseMaterial material) {
        if (material.getId() == null) {
            materialMapper.insert(material);
        } else {
            materialMapper.updateById(material);
        }
    }

    public void deleteMaterial(Long id) {
        materialMapper.deleteById(id);
    }

    public Map<Long, String> teacherNames() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getRole, Constants.ROLE_TEACHER))
                .stream().collect(Collectors.toMap(User::getId, User::getRealName));
    }
}
