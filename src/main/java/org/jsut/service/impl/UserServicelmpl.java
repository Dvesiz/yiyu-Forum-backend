package org.jsut.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import jakarta.mail.internet.MimeMessage;
import org.jsut.mapper.UserMapper;
import org.jsut.pojo.User;
import org.jsut.pojo.UserVO;
import org.jsut.service.UserService;
import org.jsut.utils.JwtUtil;
import org.jsut.utils.Md5Util;
import org.jsut.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@Service
public class UserServicelmpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Autowired
    private JavaMailSender mailSender;


    @Value("${resend.from-email}")
    private String fromEmail;
    @Override
    public User findByUsername(String username) {
        User u = userMapper.findByUsername(username);
        return u;
    }

    @Override
    public void register(String username, String password, String email) {
        //加密
        String md5Pwd = Md5Util.getMD5String(password);
        //添加
        userMapper.add(username, md5Pwd, email);
    }

    @Override
    public void update(UserVO user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl){

        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        userMapper.updateAvatar(avatarUrl, id);
    }

    @Override
    public void updatePwd(String oldPwd, String newPwd){
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd), id);
    }

    @Override
    public void delete(){
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer uid = (Integer) map.get("id");
        userMapper.delete(uid);
    }


    @Override
    public void sendLoginCode(String email) {
        // 生成 6 位验证码
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // 存入 Redis（5分钟有效）
        stringRedisTemplate.opsForValue().set("login:code:" + email, code, 5, TimeUnit.MINUTES);
        System.out.println("发送时保存的验证码 = " + code);
        System.out.println("Redis 查询结果 = " + stringRedisTemplate.opsForValue().get("login:code:" + email));

        // 邮件内容
        String html = "<div style='font-family: sans-serif; padding: 20px;'>" +
                "<h2>欢迎回来！</h2>" +
                "<p>您的登录验证码是：</p>" +
                "<h1 style='color: #4f46e5; letter-spacing: 5px;'>" + code + "</h1>" +
                "<p>验证码 <strong>5 分钟</strong> 内有效，请勿泄露给他人。</p>" +
                "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("2207547110@qq.com");
            helper.setTo(email);
            helper.setSubject("【亿语论坛】登录验证码");
            helper.setText(html, true); // 启用 HTML

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    // === 新增：邮箱登录逻辑 ===
    @Override
    public String loginByEmail(String email, String code) {
        // 1. 从 Redis 取出验证码进行比对
        String redisCode = stringRedisTemplate.opsForValue().get("login:code:" + email);

        if (redisCode == null || !redisCode.equals(code)) {
            throw new RuntimeException("验证码错误或已失效");
        }

        // 2. 获取用户信息
        User user = userMapper.findByEmail(email);
//        if (user == null) {
//            throw new RuntimeException("用户不存在");
//        }

        // 3. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = JwtUtil.genToken(claims);

        // 4. 将 Token 存入 Redis (保持和你原有登录逻辑一致)
        stringRedisTemplate.opsForValue().set(token, token, 12, TimeUnit.HOURS);

        // 5. 登录成功后，删除验证码，防止重复使用
        stringRedisTemplate.delete("login:code:" + email);

        return token;
    }
}



