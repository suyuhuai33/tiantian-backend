package edu.hebeu.partnermatching.model.domain.request;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class TeamAddRequest implements Serializable {
    private static final long serialVersionUID = -3485051141506948519L;


    /**
     * 队伍名
     */
    private String teamName;

    /**
     * 描述
     */
    private String teamDescription;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer teamStatus;

    /**
     * 密码
     */
    private String teamPassword;

}
