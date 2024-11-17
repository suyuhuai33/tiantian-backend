package edu.hebeu.partnermatching.model.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;
@Data
public class UserVO {
    /**
     * id 主键
     */
    private long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     * 0是男
     */
    private Integer gender;


    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态,0:正常
     */
    private Integer userStatus;

    /**
     * 电话
     */
    private String phone;


    /**
     * 用户角色 0 普通 1 管理员
     */
    private Integer userRole;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    private String tags;

    /**
     * 个人简介
     */
    private String profile;
}
