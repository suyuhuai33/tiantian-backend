package edu.hebeu.partnermatching.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: su
 * @Description:    与Excel表对应的信息
 */
@Data
public class ExcelTableInfo{
    /**
     * id
     */
    @ExcelProperty(value = "学号")
    private String studentId;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "姓名")
    private String name;

    @ExcelProperty(value = "劳动课成绩")
    private Integer score;

    @ExcelProperty(value = "劳动实践课成绩加分")
    private String extraScore ;

}
