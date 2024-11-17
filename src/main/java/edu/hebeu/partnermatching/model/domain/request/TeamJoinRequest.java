package edu.hebeu.partnermatching.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -2656374770011610424L;
    /**
     * 队伍id
     */
    private Long id;

    /**
     * 密码
     */
    private String teamPassword;

}
