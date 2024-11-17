package edu.hebeu.partnermatching.model.domain.request;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import edu.hebeu.partnermatching.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQueryRequest extends PageRequest {
    private static final long serialVersionUID = -5517009216614498378L;
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
     * 创建人id
     */
    private Long userId;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer teamStatus;

    /**
     * id 列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;


}
