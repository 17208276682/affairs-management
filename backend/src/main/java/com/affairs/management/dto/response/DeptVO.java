package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 部门 VO
 */
@Data
public class DeptVO {
    private String id;
    private String name;
    private String parentId;
    private String leaderId;
    private String leaderName;
    private Integer sort;
    private Integer level;
    private Integer memberCount;
    private Integer status;
    private String createdAt;
    private String updatedAt;
    private List<DeptVO> children;
}
