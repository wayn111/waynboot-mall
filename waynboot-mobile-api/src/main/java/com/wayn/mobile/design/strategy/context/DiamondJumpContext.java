package com.wayn.mobile.design.strategy.context;

import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 金刚位跳转策略配置
 */
@Component
public class DiamondJumpContext {

    private Map<Integer, DiamondJumpType> map = new HashMap<>();

    public DiamondJumpContext(List<DiamondJumpType> diamondJumpTypes) {
        for (DiamondJumpType diamondJumpType : diamondJumpTypes) {
            map.put(diamondJumpType.getType(), diamondJumpType);
        }
    }

    public DiamondJumpType getInstance(Integer jumpType) {
        return map.get(jumpType);
    }
}
