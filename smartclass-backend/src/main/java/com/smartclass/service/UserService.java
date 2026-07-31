package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartclass.common.BusinessException;
import com.smartclass.common.Constants;
import com.smartclass.dto.UserQueryDTO;
import com.smartclass.dto.UserSaveDTO;
import com.smartclass.entity.Clazz;
import com.smartclass.entity.User;
import com.smartclass.mapper.ClazzMapper;
import com.smartclass.mapper.UserMapper;
import com.smartclass.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理服务(管理端)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final ClazzMapper clazzMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Page<UserVO> page(UserQueryDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(dto.getKeyword()), User::getRealName, dto.getKeyword())
                .or()
                .like(StringUtils.hasText(dto.getKeyword()), User::getUsername, dto.getKeyword())
                .eq(StringUtils.hasText(dto.getRole()), User::getRole, dto.getRole())
                .eq(dto.getClazzId() != null, User::getClazzId, dto.getClazzId())
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .orderByDesc(User::getCreateTime);

        // keyword 为空时上面的 or() 会破坏条件结构,这里重新构建
        if (!StringUtils.hasText(dto.getKeyword())) {
            wrapper = new LambdaQueryWrapper<User>()
                    .eq(StringUtils.hasText(dto.getRole()), User::getRole, dto.getRole())
                    .eq(dto.getClazzId() != null, User::getClazzId, dto.getClazzId())
                    .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                    .orderByDesc(User::getCreateTime);
        }

        Page<User> page = userMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
        Map<Long, String> clazzNames = clazzNames();
        List<UserVO> records = page.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setClazzName(clazzNames.get(user.getClazzId()));
            return vo;
        }).collect(Collectors.toList());

        Page<UserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    public void save(UserSaveDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .ne(dto.getId() != null, User::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (dto.getId() == null) {
            if (!StringUtils.hasText(dto.getPassword())) {
                throw new BusinessException("新增用户必须填写密码");
            }
            User user = new User();
            BeanUtils.copyProperties(dto, user);
            user.setPassword(encoder.encode(dto.getPassword()));
            user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            User user = new User();
            BeanUtils.copyProperties(dto, user);
            if (StringUtils.hasText(dto.getPassword())) {
                user.setPassword(encoder.encode(dto.getPassword()));
            } else {
                user.setPassword(null);
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    public void resetPassword(Long id, String password) {
        User user = new User();
        user.setId(id);
        user.setPassword(encoder.encode(password));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public void changeStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user != null && Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("管理员账号不允许删除");
        }
        userMapper.deleteById(id);
    }

    public List<User> teachers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Constants.ROLE_TEACHER)
                .eq(User::getStatus, 1));
    }

    public List<User> students() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Constants.ROLE_STUDENT)
                .eq(User::getStatus, 1));
    }

    public List<User> byClass(Long clazzId) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getClazzId, clazzId)
                .eq(User::getStatus, 1));
    }

    private Map<Long, String> clazzNames() {
        return clazzMapper.selectList(null).stream()
                .collect(Collectors.toMap(Clazz::getId, Clazz::getName));
    }
}
