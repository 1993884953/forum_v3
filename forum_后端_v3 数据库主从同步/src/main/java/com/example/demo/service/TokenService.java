package com.example.demo.service;

import com.example.demo.dto.EmailDto;
import com.example.demo.dto.TokenCreateDto;
import com.example.demo.entity.Token;
import com.example.demo.entity.User;
import com.example.demo.interceptor.UserContext;
import com.example.demo.mapper.TokenMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class TokenService {
    @Resource
    TokenMapper tokenMapper;
    @Resource
    UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    //创建token，返回token数据
    public Map<String, String> create(TokenCreateDto tokenCreateDto, HttpServletRequest request) throws IOException, TimeoutException {
        //接收参数并转化
        User user = new User();
        BeanUtils.copyProperties(tokenCreateDto, user);

        //校验账号密码
        if (userMapper.find(user) == null) {
            throw new RuntimeException("账号或密码不匹配");
        }
        //获取从数据库加载回来的user
        BeanUtils.copyProperties(userMapper.find(user), user);

        //创建token，返回token数据
//        System.out.println("删除了" + tokenMapper.delete(user.getId()) + "条token");

        if (user.getEmail() != null) {
            EmailService.createRabbitMQ(EmailDto.builder()
                    .routingKey(user.getEmail())
                    .content(user.getNickname() + ", 你好!\n 你的账号" + user.getUsername() + "于" + new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss").format(new Date()) + "登录代码库,IP地址为" + getIpAddress(request) + "，如果非本人操作，请及时修改密码！")
                    .build());
        }


        String tokenStr = UUID.randomUUID().toString().replaceAll("-", "");
        Token token = Token.builder().token(tokenStr).userId(user.getId()).build();
        if (tokenMapper.create(token) != 1) {
            throw new RuntimeException("token创建失败");
        }

        System.out.println(user);
        //创建map类型data数据
        Map<String, String> data = new HashMap<>();
        data.put("token", tokenStr);
        return data;
    }


    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    //删除token
    public Map<String, String> delete(String token) {
        if (token == null || token.length() != 32) {
            throw new RuntimeException("token格式不正确");
        }
        if (tokenMapper.find(token) != UserContext.getUser().getId()) {
            throw new RuntimeException("无效token");
        }
        if (tokenMapper.delete(UserContext.getUser().getId()) == 0) {
            throw new RuntimeException("token删除失败");
        }
        System.out.println(UserContext.getUser());
        return null;
    }
    public  String emailUpdate( EmailDto emailDto){
        User user=  userMapper.find(User.builder()
                .username(emailDto.getUsername())
                .password(emailDto.getPassword())
                .build());
        if (user==null){throw new RuntimeException("账号或密码不匹配");}
        EmailDto  emailHistory= (EmailDto) redisTemplate.opsForHash().get("emailKey", emailDto.getEmail());
        if (emailDto.getKey()==null) {
            throw new RuntimeException("请输入验证吗");
        }
        if (emailHistory==null||!emailDto.getKey().equals(emailHistory.getKey())) {throw new RuntimeException("验证码错误");}

        user.setEmail(emailDto.getEmail());
        if (userMapper.update(user)!=1) {
            throw new RuntimeException("更新失败");
        }
        UserContext.setUser(user);
        return user.getEmail();
    }

    public String emailKeyCreate(EmailDto emailDto) throws IOException, TimeoutException {
        User user=  userMapper.find(User.builder()
                .username(emailDto.getUsername())
                .password(emailDto.getPassword())
                .build());
        if (user==null){throw new RuntimeException("账号或密码不匹配");}
        emailDto.setKey(getRandom());
        emailDto.setDate( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        redisTemplate.opsForHash().put("emailKey", emailDto.getEmail(), emailDto);

        if (user.getEmail() != null) {
            EmailService.createRabbitMQ(EmailDto.builder()
                    .routingKey(emailDto.getEmail())
                    .content("你好!" + "\n 你的账号" + emailDto.getUsername() + "于" +emailDto.getDate() + "申请绑定邮箱, "+"如果非本人操作请忽略\n\r\n验证码为:\n\r\n"+emailDto.getKey())
                    .build());
        }
        return emailDto.getKey();
    }
    public static String getRandom(){
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int r = random.nextInt(10); //每次随机出一个数字（0-9）
            code.append(r);  //把每次随机出的数字拼在一起
        }
        return code.toString();
    }
}
