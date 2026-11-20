package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security用户详情服务
 * 根据用户名从user表加载用户，供JWT过滤器校验Token使用
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        String role = user.getRole() == null ? "patient" : user.getRole();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPwd())
                .disabled(user.getStatus() == null || user.getStatus() != 1)
                // roles()会自动补上ROLE_前缀，与hasRole("ADMIN")对应
                .roles(role.toUpperCase())
                .build();
    }
}
