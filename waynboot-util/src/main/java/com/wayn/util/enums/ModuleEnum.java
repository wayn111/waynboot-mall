package com.wayn.util.enums;

import lombok.Getter;

@Getter
public enum ModuleEnum {

    DEFAULT(""),
    USER("用户模块"),
    ROLE("角色模块"),
    DEPT("部门模块"),
    MENU("菜单模块"),
    DICT("字典模块"),


    ;
    private String name;

    ModuleEnum(String name) {
        this.name = name;
    }
}
