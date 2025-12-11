package org.jsut.service;

import org.jsut.pojo.User;
import org.jsut.pojo.UserVO;

public interface UserService {
    //根据用户名查询用户
    User findByUsername(String username);
    // 注册
    void register(String username, String password , String email);
    // 更新
    void update(UserVO user);
    // 更新头像
    void updateAvatar(String avatarUrl);
    // 更新密码
    void updatePwd(String username, String newPwd);
    // 删除用户
    void delete();

    // 发送登录验证码
    void sendLoginCode(String email);

    // 邮箱验证码登录，返回 Token
    String loginByEmail(String email, String code);
}
