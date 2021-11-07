package com.lhalj.emos.api.controller.from;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel
@Data
public class SearchMembersForm {

    @NotBlank
    private String members;
}
