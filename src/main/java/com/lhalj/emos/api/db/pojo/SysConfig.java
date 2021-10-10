package com.lhalj.emos.api.db.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * sys_config
 * @author 
 */
@ApiModel(value="com.lhalj.emos.api.db.pojo.SysConfig")
@Data
public class SysConfig implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value="主键")
    private Integer id;

    /**
     * 参数名
     */
    @ApiModelProperty(value="参数名")
    private String paramKey;

    /**
     * 参数值
     */
    @ApiModelProperty(value="参数值")
    private String paramValue;

    /**
     * 状态
     */
    @ApiModelProperty(value="状态")
    private Boolean status;

    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}