package com.wayn.project.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayn.project.system.domain.SysDept;
import com.wayn.project.system.domain.SysMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TreeVO {
    /**
     * 节点id
     */
    private long id;

    /**
     * 节点显示名称
     */
    private String label;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeVO> children = new ArrayList<>();

    public TreeVO(SysMenu menu) {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeVO::new).collect(Collectors.toList());
    }

    public TreeVO(SysDept dept) {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.children = dept.getChildren().stream().map(TreeVO::new).collect(Collectors.toList());
    }

    public TreeVO() {
    }
}
