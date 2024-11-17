package edu.hebeu.partnermatching.common;

/**
 * 返回工具類
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success(T data){
        return new  BaseResponse<>(0,data,"ok");
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new  BaseResponse<>(errorCode.getCode(),null, errorCode.getMessage(), errorCode.getDdescrip());
    }

    public static <T> BaseResponse<T> error(int code,String message,String description){
        return new  BaseResponse<>(code,null,message,description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode,String message){
        return new  BaseResponse<>(errorCode.getCode(),null,message,"");
    }
}
