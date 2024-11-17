package edu.hebeu.partnermatching.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelet;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}