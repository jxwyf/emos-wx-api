package com.lhalj.emos.api.exception;

import lombok.Data;

/**
 * 描述: 全局异常类
 */
@Data
public class EmosException extends RuntimeException{

    // 消息
    private String msg;
    //状态码
    private int code;

    public EmosException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public EmosException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public EmosException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }


    public EmosException(String msg, Throwable e, int code) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }

}
