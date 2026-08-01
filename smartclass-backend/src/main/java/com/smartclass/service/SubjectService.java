package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.entity.Subject;
import com.smartclass.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学科管理服务
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectMapper subjectMapper;

    public List<Subject> list() {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>().orderByAsc(Subject::getId));
    }

    public void save(Subject subject) {
        if (subject.getId() == null) {
            subjectMapper.insert(subject);
        } else {
            subjectMapper.updateById(subject);
        }
    }

    public void delete(Long id) {
        subjectMapper.deleteById(id);
    }

    public Map<Long, String> nameMap() {
        return subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
    }
}
