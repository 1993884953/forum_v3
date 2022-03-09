package com.example.demo.util;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//帖子对应的信息
public class PostResult<T> implements Serializable {
    private int userId;
    private String username;
    private String nickname;
    private String avatar;

    private Boolean like;
    private Boolean favorite;
    private int level;

    private T post;
}
//      "avatar": "string",
//      "favorite": true,
//      "level": 0,
//      "like": true,

//      "nickname": "string",
//      "userId": 0,
//      "username": "string"