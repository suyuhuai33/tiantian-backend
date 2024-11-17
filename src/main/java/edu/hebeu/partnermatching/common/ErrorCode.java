package edu.hebeu.partnermatching.common;

/**
 * 錯誤碼
 */
public enum ErrorCode {
    Success(0,"ok","成功"),

    PARAMS_ERROR(40000,"請求參數錯誤",""),

    NULL_ERROR(40001,"請求參數為空",""),

    NOT_LOGIN(40100,"未登入",""),

    NOT_AUTH(40101,"無權限",""),

    SYSTEM_ERROR(40200,"系統内部異常","");

    private final int code;

    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDdescrip() {
        return ddescrip;
    }

    private final String ddescrip;

    ErrorCode(int code, String message, String ddescrip) {
        this.code = code;
        this.message = message;
        this.ddescrip = ddescrip;
    }
}
