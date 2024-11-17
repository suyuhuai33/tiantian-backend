package edu.hebeu.partnermatching.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础页码
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 7802429513154027649L;
    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 当前页码
     */
    private int pageNum = 1;
}
