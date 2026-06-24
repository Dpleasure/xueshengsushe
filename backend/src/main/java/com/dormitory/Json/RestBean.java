package com.dormitory.Json;

import lombok.Data;

@Data
public class RestBean<T>{
    private int status;
    private  boolean success;
    private T message;

    public RestBean( int status,boolean success, T message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public static <T>RestBean<T> success()
   {
       return new RestBean<T>(200,true,null);
   }
    public static <T>RestBean<T> success(T data)
    {
        return new RestBean<T>(200,true,data);
    }
    public static <T>RestBean<T> failure(int status)
    {
        return new RestBean<T>(status,false,null);
    }
    
    public static <T>RestBean<T> failure(int status,T data)
    {
        return new RestBean<T>(status,false,data);
    }
    
    // 便捷方法：使用默认404
    public static <T>RestBean<T> failure(String message) {
        return new RestBean<T>(401, false, (T) message);
    }
    
    // 便捷方法：使用默认500
    public static <T>RestBean<T> serverError(String message) {
        return new RestBean<T>(401, false, (T) message);
    }
}
