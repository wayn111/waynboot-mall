package com.wayn.common.core.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.domain.system.Menu;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {
    List<String> selectMenuPermsByUserId(Long userId);

    List<Menu> selectMenuTreeByUserId(Long userId);

    List<Menu> selectMenuTreeAll();

    List<Menu> selectMenuListByUserId(Menu menu, Long userId);

    List<Menu> selectMenuList(Menu menu);
}
