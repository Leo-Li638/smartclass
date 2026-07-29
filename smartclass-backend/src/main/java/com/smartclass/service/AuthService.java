package com.smartclass.service;

import com.smartclass.common.BusinessException;
import com.smartclass.common.Constants;
import com.smartclass.dto.LoginDTO;
import com.smartclass.dto.PasswordDTO;
import com.smartclass.dto.RegisterDTO;
import com.smartclass.entity.User;
import com.smartclass.mapper.UserMapper;
import com.smartclass.util.JwtUtil;
import com.smartclass.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务:登录、注册、个人资料、密码修改
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被停用,请联系管理员");
        }
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .avatarText(user.getRealName() == null ? "?" : user.getRealName().substring(0, 1))
                .build();
    }

    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(Constants.ROLE_STUDENT);
        user.setClazzId(dto.getClazzId());
        user.setGender(dto.getGender());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    public User currentUser() {
        Long userId = com.smartclass.util.UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public void changePassword(PasswordDTO dto) {
        User user = currentUser();
        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(encoder.encode(dto.getNewPassword()));
        update.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);
    }
}
