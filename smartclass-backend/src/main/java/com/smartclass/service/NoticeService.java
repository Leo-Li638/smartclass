package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.Constants;
import com.smartclass.entity.Notice;
import com.smartclass.entity.User;
import com.smartclass.mapper.NoticeMapper;
import com.smartclass.mapper.UserMapper;
import com.smartclass.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务:管理员发布,各角色按可见范围查询
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    public List<Notice> listForCurrent() {
        String role = UserContext.getRole();
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .in(Notice::getTargetRole, "ALL", role)
                .orderByDesc(Notice::getCreateTime));
    }

    public List<Notice> listAll() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getCreateTime));
    }

    public void save(Notice notice) {
        notice.setPublisherId(UserContext.getUserId());
        if (notice.getId() == null) {
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
    }

    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }

    public String publisherName(Long publisherId) {
        if (publisherId == null) {
            return "系统";
        }
        User user = userMapper.selectById(publisherId);
        return user == null ? "系统" : user.getRealName();
    }
}
