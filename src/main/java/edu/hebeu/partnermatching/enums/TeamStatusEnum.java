package edu.hebeu.partnermatching.enums;

import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.exception.BusinessException;

public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    Encrypt(2, "加密");

    private int value;

    private String text;

    public static  TeamStatusEnum getEnumByValue(Integer value){
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TeamStatusEnum[] teamStatusEnum = TeamStatusEnum.values();
        for (TeamStatusEnum status: teamStatusEnum
             ) {
            if(status.getValue() == value){
                return status;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
