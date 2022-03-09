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
public class Token implements Serializable {
    private int id;
    private int userId;
    private String token;
    private String createdAt;
    private String updateAt;
}
