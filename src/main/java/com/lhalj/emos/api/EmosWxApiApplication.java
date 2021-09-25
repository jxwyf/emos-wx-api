package com.lhalj.emos.api;

import cn.hutool.core.util.StrUtil;
import com.lhalj.emos.api.config.SystemConstants;
import com.lhalj.emos.api.db.dao.SysConfigDao;
import com.lhalj.emos.api.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan //在SpringBootApplication上使用@ServletComponentScan注解后，Servlet、Filter、Listener可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册，无需其他代码。
@Slf4j
@EnableAsync
public class EmosWxApiApplication {

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants systemConstants;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }


    @PostConstruct
    public void init(){
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(one ->{
            String key = one.getParamKey();
            //把常量名字变为驼峰命名法
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue();
            //通过反射的方式给封装的对象赋值
            try{
                Field field = systemConstants.getClass().getDeclaredField(key);
                //反射 给变量赋值
                field.set(systemConstants,value);
            }catch(Exception e){
                log.error("执行异常",e);
            }
        });
    }



}
