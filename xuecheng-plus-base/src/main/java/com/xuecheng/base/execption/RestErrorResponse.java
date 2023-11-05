package com.xuecheng.base.execption;

import java.io.Serializable;

/**
 * @author fantasy
 * @description 错误响应参数
 * @date 2023/11/5 15:15
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}

