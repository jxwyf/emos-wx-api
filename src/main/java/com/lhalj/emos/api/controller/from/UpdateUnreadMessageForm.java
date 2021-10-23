package com.lhalj.emos.api.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateUnreadMessageForm {
    @NotBlank
    private String id;
}
