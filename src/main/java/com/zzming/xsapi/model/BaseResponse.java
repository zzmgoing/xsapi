package com.zzming.xsapi.model;

public class BaseResponse<T> {

    private int code;

    private String message;

    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
