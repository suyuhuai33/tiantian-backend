package edu.hebeu.partnermatching.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -5889847387148625197L;

    String userAccount;
    String userPassword;
    String checkPassword;
}
