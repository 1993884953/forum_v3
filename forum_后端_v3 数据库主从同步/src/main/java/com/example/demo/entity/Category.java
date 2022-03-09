package com.example.demo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Category  implements Serializable {
    private int id;
    private String name;
    private int status;
    private  String createdAt;
    private  String updateAt;
}
