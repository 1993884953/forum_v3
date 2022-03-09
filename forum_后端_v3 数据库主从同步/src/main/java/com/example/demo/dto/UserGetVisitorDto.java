package com.example.demo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserGetVisitorDto implements Serializable {
    private String avatar;
    private String UserName;
}
// "avatar": "string",
//"username": "string"