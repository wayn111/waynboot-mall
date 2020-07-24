package com.wayn.common.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayn.common.core.domain.system.Dept;
import com.wayn.common.core.domain.system.Menu;
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

    public TreeVO(Menu menu) {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeVO::new).collect(Collectors.toList());
    }

    public TreeVO(Dept dept) {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.children = dept.getChildren().stream().map(TreeVO::new).collect(Collectors.toList());
    }

    public TreeVO() {
    }
}
