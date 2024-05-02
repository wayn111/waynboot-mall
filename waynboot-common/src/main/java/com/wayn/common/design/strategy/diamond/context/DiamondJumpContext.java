package com.wayn.common.design.strategy.diamond.context;

import com.wayn.common.design.strategy.diamond.strategy.DiamondJumpTypeInterface;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 金刚位跳转策略配置
 */
@Component
public class DiamondJumpContext {

    private final Map<Integer, DiamondJumpTypeInterface> map = new HashMap<>();

    /**
     * 由spring自动注入DiamondJumpType子类
     *
     * @param diamondJumpTypeInterfaces 金刚位跳转类型集合
     */
    public DiamondJumpContext(List<DiamondJumpTypeInterface> diamondJumpTypeInterfaces) {
        for (DiamondJumpTypeInterface diamondJumpTypeInterface : diamondJumpTypeInterfaces) {
            map.put(diamondJumpTypeInterface.getType(), diamondJumpTypeInterface);
        }
    }

    public DiamondJumpTypeInterface getInstance(Integer jumpType) {
        return map.get(jumpType);
    }
}
