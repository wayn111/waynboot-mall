package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.message.core.dto.OrderDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSubmitChainTest {

    /**
     * 验证责任链会按步骤声明的顺序执行，避免构造顺序影响下单链路。
     */
    @Test
    void executeRunsSupportedStepsByDeclaredOrder() {
        List<String> executedSteps = new ArrayList<>();
        OrderSubmitStep secondStep = new RecordingStep(200, context -> executedSteps.add("second"));
        OrderSubmitStep firstStep = new RecordingStep(100, context -> executedSteps.add("first"));
        OrderSubmitChain chain = new OrderSubmitChain(List.of(secondStep, firstStep));

        chain.execute(OrderSubmitChainContext.single(buildOrderDTO(), ignored -> null));

        assertEquals(List.of("first", "second"), executedSteps);
    }

    /**
     * 验证前置步骤判定订单已存在后会中断后续扣库存、落库等副作用步骤。
     */
    @Test
    void executeStopsRemainingStepsWhenContextIsStopped() {
        List<String> executedSteps = new ArrayList<>();
        OrderSubmitStep duplicateStep = new RecordingStep(100, context -> {
            executedSteps.add("duplicate");
            context.markExistingOrder();
            context.stop("订单已存在");
        });
        OrderSubmitStep stockStep = new RecordingStep(200, context -> executedSteps.add("stock"));
        OrderSubmitChain chain = new OrderSubmitChain(List.of(duplicateStep, stockStep));

        OrderSubmitChainContext context = chain.execute(OrderSubmitChainContext.single(buildOrderDTO(), ignored -> null));

        assertEquals(List.of("duplicate"), executedSteps);
        assertTrue(context.isExistingOrder());
        assertTrue(context.isStopped());
    }

    /**
     * 构建测试用下单 DTO。
     *
     * @return 下单 DTO
     */
    private OrderDTO buildOrderDTO() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("CHAIN-SN");
        orderDTO.setUserId(100L);
        return orderDTO;
    }

    /**
     * 测试用责任链步骤。
     * 只记录执行动作，便于验证责任链排序和中断语义。
     */
    private static final class RecordingStep implements OrderSubmitStep {

        private final int order;
        private final StepAction action;

        /**
         * 构造测试步骤。
         *
         * @param order 步骤顺序
         * @param action 执行动作
         */
        private RecordingStep(int order, StepAction action) {
            this.order = order;
            this.action = action;
        }

        /**
         * 返回测试步骤顺序。
         *
         * @return 步骤顺序
         */
        @Override
        public int order() {
            return order;
        }

        /**
         * 执行测试动作。
         *
         * @param context 下单责任链上下文
         */
        @Override
        public void execute(OrderSubmitChainContext context) {
            action.execute(context);
        }
    }

    /**
     * 测试用步骤动作。
     */
    @FunctionalInterface
    private interface StepAction {

        /**
         * 执行步骤动作。
         *
         * @param context 下单责任链上下文
         */
        void execute(OrderSubmitChainContext context);
    }
}
