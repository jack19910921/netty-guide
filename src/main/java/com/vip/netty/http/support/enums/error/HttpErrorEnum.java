package com.vip.netty.http.support.enums.error;

/**
 * Created by jack on 16/8/8.
 */
public enum HttpErrorEnum implements AbstractErrorEnum {
    REFLECT_ERROR("50000", "JavaBean2Map时,反射异常");


    private String errorCode;
    private String errorMessage;

    HttpErrorEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
