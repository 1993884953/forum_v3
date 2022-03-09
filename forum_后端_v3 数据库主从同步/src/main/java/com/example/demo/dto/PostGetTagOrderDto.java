package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGetTagOrderDto implements Serializable {
    private int id;
    private String name;
    private int quantity;
    private String createdAt;
    private String updateAt;
}