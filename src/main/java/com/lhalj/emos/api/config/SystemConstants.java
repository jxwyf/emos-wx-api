package com.lhalj.emos.api.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 描述:考勤时间配置类
 */
@Data
@Component
public class SystemConstants {

    public String attendanceStartTime;
    public String attendanceTime;
    public String attendanceEndTime;
    public String closingStartTime;
    public String closingTime;
    public String closingEndTime;
}
