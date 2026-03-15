package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 组织架构树节点
 */
@Data
public class OrgTreeNode {
    private String id;
    private String label;
    /** 类型：dept / member */
    private String type;
    private String parentId;
    /** 部门负责人姓名（dept类型） */
    private String leaderName;
    /** 职位（member类型） */
    private String position;
    /** 头像（member类型） */
    private String avatar;
    /** 成员数量（dept类型） */
    private Integer memberCount;
    /** 子节点 */
    private List<OrgTreeNode> children;
}
