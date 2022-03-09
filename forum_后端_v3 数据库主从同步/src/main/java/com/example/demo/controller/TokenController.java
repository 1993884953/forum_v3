package com.example.demo.controller;

import com.example.demo.dto.EmailDto;
import com.example.demo.dto.TokenCreateDto;
import com.example.demo.interceptor.UserContext;
import com.example.demo.service.TokenService;
import com.example.demo.util.JsonResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@CrossOrigin
@RestController
public class TokenController {
    @Resource
    TokenService tokenService;
    //用户登录
    @PostMapping("api/token/create")
    public JsonResult<Map<String,String>> create(@Valid TokenCreateDto tokenCreateDto, HttpServletRequest request) throws IOException, TimeoutException {
        Map<String,String>data=  tokenService.create(tokenCreateDto,  request);
        return  new JsonResult<>("获取token成功",data);
    }
    //删除token
    @GetMapping("api/token/delete")
    public JsonResult<Map<String,String>> delete(String token){
        System.out.println(tokenService.delete(token));
        return  new JsonResult<>("删除token成功");
    }
    //设置邮箱
    @PostMapping("/api/email/update")
    public JsonResult<String> updateEmail(@Valid EmailDto emailDto){
        return new JsonResult<>(tokenService.emailUpdate(emailDto));
    }
    //设置邮箱
    @PostMapping("/api/email/key")
    public JsonResult<String> emailKeyCreate(@Valid EmailDto emailDto) throws IOException, TimeoutException {
        return new JsonResult<>(tokenService.emailKeyCreate(emailDto));
    }

}
