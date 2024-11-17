package edu.hebeu.partnermatching.model.domain.request;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName team
 */
@TableName(value ="team")
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = 7364078986237888892L;
    /**
     * id
     */

    private Long id;

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
     * 0-公开 1-私有 2-加密
     */
    private Integer teamStatus;

    /**
     * 密码
     */
    private String teamPassword;

    
}