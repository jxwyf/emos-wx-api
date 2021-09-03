package com.lhalj.emos.api.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 描述:
 */
@ApiModel
@Data
public class LoginForm {

    @NotBlank(message = "临时授权不能为空")
    private String code;
}
