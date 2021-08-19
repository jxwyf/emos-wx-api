package com.lhalj.emos.api.config.xss;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述: 抵御XSS攻击 重写四种方法
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }


    @Override
    public String getParameter(String name) {
        //保存接收到的数据
        String value = super.getParameter(name);
        //判断是否为空
        if(!StrUtil.hasBlank(value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values =  super.getParameterValues(name);
        //判断数组是否为空
        if (values!=null) {
            //遍历数组
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                //判断数据是否有效 有效就转义
                if(!StrUtil.hasBlank(value)){
                    value = HtmlUtil.filter(value);
                }
                // 数据放回数组
                values[i] = value;
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        //定义一个map 存放转义的数据
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameters != null) {
            //遍历
            for (String key:parameters.keySet()) {
                String[] values = parameters.get(key);
                //遍历数组
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    //判断数据是否有效 有效就转义
                    if(!StrUtil.hasBlank(value)){
                        value = HtmlUtil.filter(value);
                    }
                    // 数据放回数组
                    values[i] = value;
                }
                map.put(key, values);
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value =  super.getHeader(name);
        //判断数据是否有效 有效就转义
        if(!StrUtil.hasBlank(value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in = super.getInputStream();
        //读取字符流
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        //缓冲流
        BufferedReader buff = new BufferedReader(reader);
        //字符串拼接
        StringBuffer body = new StringBuffer();

        String line = buff.readLine();
        while (line != null){
            body.append(line);
            //读取下一行的数据
            line = buff.readLine();
        }
        buff.close();
        reader.close();
        in.close();
        //数据类型转换
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        //对map对象转义
        Map<String, Object> result = new LinkedHashMap<>();
        //
        for(String key:map.keySet()){
            Object val = map.get(key);
            //判断是否是字符串格式的数据
            if(val instanceof String){
                if (!StrUtil.hasEmpty(val.toString())) {
                    result.put(key, HtmlUtil.filter(val.toString()));
                }
            }else {
                //不是字符串格式
                result.put(key, val);
            }
        }
        String json = JSONUtil.toJsonStr(result);
        ByteArrayInputStream bain = new ByteArrayInputStream(json.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bain.read();
            }
        };
    }
}
