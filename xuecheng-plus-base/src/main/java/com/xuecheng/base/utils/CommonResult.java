package com.xuecheng.base.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fantasy
 * @description TODO
 * @date 2023/11/7 19:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T>{
    private Integer code;
    private String message;
    private T data;

    public static CommonResult success(){
        CommonResult result=new CommonResult();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static <T> CommonResult<T> success(T data){
        CommonResult<T> result=new CommonResult<T>();
        result.setData(data);
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static CommonResult error(Integer code,String msg){
        CommonResult result=new CommonResult();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }
}