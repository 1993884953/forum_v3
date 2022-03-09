package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {
    private int id;
    private String username;
    private String nickname;
    private String password;
    private String avatar;
    private int level;
    private String mobile;
    private String email;
    private String createdAt;
    private String updateAt;
}
