package com.example.demo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PostCreatedReplyDto  implements Serializable {
    @NotNull(message = "id不能为空")
    private Integer id;
    @NotBlank
    private String content;
}
