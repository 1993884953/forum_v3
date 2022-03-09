package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Token;
import com.example.demo.entity.User;
import com.example.demo.interceptor.UserContext;
import com.example.demo.mapper.TokenMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.Image;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

//解决跨域问题
@CrossOrigin
@Service
public class UserService {
    @Resource
    UserMapper userMapper;
    @Resource
    TokenService tokenService;
    @Resource
    TokenMapper tokenMapper;

    //用户注册
    public Map<String, String> create(UserCreateDto userCreateDto, HttpServletRequest request) throws IOException, TimeoutException {
        //接收传入参数
        userCreateDto.setAvatar(Image.getAvatar());
        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        //获取数据库数据
        if (userMapper.find(user) != null) {
            throw new RuntimeException("用户名已存在");
        }
        BeanUtils.copyProperties(userCreateDto, user);

        //user表创建user信息
        if (userMapper.create(user) != 1) {
            throw new RuntimeException("用户创建失败");
        }
        TokenCreateDto tokenCreateDto = new TokenCreateDto();
        BeanUtils.copyProperties(user, tokenCreateDto);
        //创建返回数据
        return tokenService.create(tokenCreateDto,  request);
    }

    //更新用户信息
    public void update(UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getPassword() == null && userUpdateDto.getMobile() == null && userUpdateDto.getNickname() == null) {
            throw new RuntimeException("未更新任何数据");
        }
        if (!Objects.equals(userUpdateDto.getPassword(), userUpdateDto.getOldPassword())) {
            System.out.println(userUpdateDto.getPassword());System.out.println(userUpdateDto.getOldPassword());
            throw new RuntimeException("两次密码输入不一致");

        }
        if (UserContext.getUser() == null) {
            throw new RuntimeException("更新信息失败");
        }
        userUpdateDto.setId(UserContext.getUser().getId());
        User user = new User();
        BeanUtils.copyProperties(userUpdateDto, user);
        if (userMapper.update(user) != 1) {
            throw new RuntimeException("数据库更新失败");
        }
    }

    //通过用户上下文获取用户信息
    public UserGetUserDto getUser(String token) {
        if (token.length() != 32) {
            throw new RuntimeException("token格式错误");
        }
       Integer userId=tokenMapper.find(token);
        if (userId==null){
            throw new RuntimeException("token 失效或已过期");
        }
       User user= userMapper.find(User.builder().id(userId).build());
        if (user ==null){
            throw new RuntimeException("用户状态异常");
        }
        UserGetUserDto userGetUserDto=new UserGetUserDto();
        BeanUtils.copyProperties(user, userGetUserDto);
        return userGetUserDto;
    }

    //通过user表的创建时间，获取近访问
    @Cacheable(cacheNames = "getVisitor", key = "")
    public List<UserGetVisitorDto> getVisitor() {
        List<UserGetVisitorDto> data = new ArrayList<>();
        System.out.println(userMapper.getVisitor());
        userMapper.getVisitor().forEach(item -> {
            UserGetVisitorDto userGetVisitorDto = new UserGetVisitorDto();
            BeanUtils.copyProperties(item, userGetVisitorDto);
            data.add(userGetVisitorDto);
        });
        return data;
    }

    //配置文件注入
    @Value("${upload.path}")
    private String uploadPath;
    @Value("${server.port}")
    private String host;
    @Value("${spring.mvc.static-path-pattern}")
    private String uploads;
//    @PostMapping("/api/upload/avatar")//传入数据，设置请求参数
    //请求时选中对应的类型，
    public Map<String, String> avatar(MultipartFile file) throws IOException {
//        System.out.println(file.getOriginalFilename());
        //上传检测
        if (file.isEmpty()) {
            throw new RuntimeException("文件未上传");
        }
        //获取文件后缀
        int index = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
        String extname = file.getOriginalFilename().substring(index + 1).toLowerCase();
        //判断上传格式
        String allowImgFormat = "png,jpg,jpeg,gif";
        if (!allowImgFormat.contains(extname)) {
            throw new RuntimeException("文件类型不被允许");
        }
        System.out.println(file.getOriginalFilename());
        //拼接文件夹
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/");
        String subPath = simpleDateFormat.format(new Date());
        //拼接文件名
        String saveName = subPath + subPath.replaceAll("/", "-") + UUID.randomUUID().toString().replaceAll("-", "") + "." + extname;
        //String uploadPath="uploads/";
        System.out.println(uploadPath);
        System.out.println(subPath);
        //创建文件夹与文件名
        File dir = new File(uploadPath + subPath);
        if (!dir.exists()) {
            //文件夹创建状态
            if (!dir.mkdirs()) {
                throw new RuntimeException("文件夹创建失败");
            }
        }
        //设置保存文件的路径
        File save = new File(uploadPath + saveName);
        //获取绝对地址
        //System.out.println(save.getAbsoluteFile());
        file.transferTo(save.getAbsoluteFile());
        //创建http
        String http = "http://localhost:" + host + uploads.replaceAll("[*]", "") + saveName;
        User user = User.builder().avatar(http).id(UserContext.getUser().getId()).build();
        System.out.println(user);
        userMapper.update(user);
        Map<String, String> data = new HashMap<>();
        data.put("头像地址上传成功", http);
        return data;
    }

    //查user表
    public User getUserById(User user){
        User findUser=userMapper.find(user);
        if (findUser==null){
            return user;
        }
        return  findUser;
    }

    @Cacheable(cacheNames = "findUserByToken",  key = "'token'+#token")
    public  User findUserByToken(String token){

        //去token表查，得到userId
        Integer userId=tokenMapper.find(token);
        if (userId==null){
            throw new RuntimeException("无效的token");
        }
        System.out.println("拦截器生效,token:" + userId);
        //去user表查，得到user类
        User user=userMapper.find(User.builder().id(userId).build());
        //查到则储存User信息
        if (user==null){
            throw new RuntimeException("用户不存在或者已经注销");
        }

        return user;
    }


}
