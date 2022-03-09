package com.example.demo.dto;
import lombok.Data;

import java.io.Serializable;

@Data

public class UserGetUserDto implements Serializable {
    private int id;
    private String username;
    private String nickname;
    private String mobile;
    private String avatar;
    private String email;
    private String createdAt;
    private int level;
}
