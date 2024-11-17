package edu.hebeu.partnermatching.model.domain.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 4201555333767949466L;

    String userAccount;
    String userPassword;
}
