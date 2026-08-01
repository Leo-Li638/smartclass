package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.BusinessException;
import com.smartclass.entity.Clazz;
import com.smartclass.entity.User;
import com.smartclass.mapper.ClazzMapper;
import com.smartclass.mapper.UserMapper;
import com.smartclass.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 班级管理服务
 */
@Service
@RequiredArgsConstructor
public class ClazzService {

    private final ClazzMapper clazzMapper;
    private final UserMapper userMapper;

    public List<Clazz> list() {
        return clazzMapper.selectList(new LambdaQueryWrapper<Clazz>().orderByAsc(Clazz::getId));
    }

    public void save(Clazz clazz) {
        if (clazz.getId() == null) {
            clazzMapper.insert(clazz);
        } else {
            clazzMapper.updateById(clazz);
        }
    }

    public void delete(Long id) {
        Long studentCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getClazzId, id));
        if (studentCount > 0) {
            throw new BusinessException("该班级下仍有 " + studentCount + " 名学生,无法删除");
        }
        clazzMapper.deleteById(id);
    }

    public Map<Long, String> nameMap() {
        return clazzMapper.selectList(null).stream()
                .collect(Collectors.toMap(Clazz::getId, Clazz::getName));
    }

    public List<NameValueVO> studentDist() {
        return list().stream().map(clazz -> {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getClazzId, clazz.getId()));
            return new NameValueVO(clazz.getName(), count);
        }).collect(Collectors.toList());
    }
}
