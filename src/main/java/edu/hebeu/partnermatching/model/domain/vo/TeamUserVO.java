package edu.hebeu.partnermatching.model.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类（脱敏）
 */
 @Data
public class TeamUserVO implements Serializable {
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
     * 创建人id
     */
    private Long userId;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer teamStatus;

    /**
     * 加入人数
     */
    private Integer hasJoinNum;

    private Date creatTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private UserVO creatUserVO;

    private Boolean hasJoin = false;
}
