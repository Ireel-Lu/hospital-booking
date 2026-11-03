package com.hospital.booking.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.booking.entity.Result;
import com.hospital.booking.entity.User;
import com.hospital.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserContoller {

    private final UserMapper userMapper;

    /**
     * 查询用户列表
     * @param keyword 搜索关键字（用户名、真实姓名、手机号）
     * @return 用户列表数据
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false)String keyword){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(keyword !=null && !keyword.trim().isEmpty()){
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        List<User> userList = userMapper.selectList(wrapper);

        return Result.success(userList);
    }

    /**
     * 添加新用户
     * @param params 用户信息参数：username、realName、pwd、phone、role
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, Object> params){
        String username = (String) params.get("username");
        String realName = (String) params.get("realName");
        String pwd = (String) params.get("pwd");
        String phone = (String) params.get("phone");
        String role = (String) params.get("role");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if(userMapper.selectCount(wrapper)>0){
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        //密码用md5加密存储
        user.setPwd(DigestUtils.md5DigestAsHex(pwd.getBytes()));
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);//默认启用状态

        userMapper.insert(user);

        return Result.success("添加成功");

    }

    /**
     * 更新用户状态
     * @param user 用户对象，包含id、status
     * @return 更新结果
     */
    @PutMapping("/status")
    public Result updateStatus(@RequestBody User user){
        if(user.getId() == null || user.getStatus() == null){
            return Result.error("参数错误");
        }

        User existingUser = userMapper.selectById(user.getId());
        if(existingUser == null){
            return Result.error("用户不存在");
        }

        existingUser.setStatus(user.getStatus());
        userMapper.updateById(existingUser);

        return Result.success("更新成功");
    }

    /**
     * 编辑用户信息
     * @param params 用户信息参数：id、username、realName、phone、role
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result update(@RequestBody Map<String,Object> params){
        Long id = Long.valueOf(params.get("id").toString());
        User user = userMapper.selectById(id);
        if(user == null){
            return Result.error("用户不存在");
        }

        //修改用户名，校检新用户名是否被其他用户占用
        if(params.get("username") != null){
            String newUsername = (String)params.get("username");
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername,newUsername);
            if(userMapper.selectCount(wrapper)>0){
                return Result.error("用户名已存在");
            }
            user.setUsername(newUsername);
        }

        if(params.get("realName") != null)user.setRealName((String)params.get("realName"));
        if(params.get("phone") != null)user.setPhone((String)params.get("phone"));
        if(params.get("role") != null)user.setRole((String)params.get("role"));

        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    /**
     * 重置用户密码
     * @param params 用户信息参数：id、newPwd
     * @return 更新结果
     */
    @PutMapping("/reset-password")
    public Result resetPwd(@RequestBody Map<String,Object> params){
        Long id = Long.valueOf(params.get("id").toString());
        User user = userMapper.selectById(id);
        if(user == null) {
            return Result.success("用户不存在");
        }

        String newPwd = (String)params.get("pwd");
        user.setPwd(newPwd);
        userMapper.updateById(user);

        return Result.success("密码重置成功");
    }

    /**
     * 删除用户
     * @param id 用户id
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

}
