package com.lhalj.emos.api.controller.from;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 描述:
 */
@Data
@ApiModel
public class CheckinForm {

    private String address;
    private String country;
    private String province;
    private String city;
    private String district;
}
