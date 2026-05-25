package com.wayn.admin.api;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 模块边界和定时任务治理约束测试。
 * 该测试不启动 Spring 容器，只读取 POM 和注解，快速防止后续重构再次引入过渡依赖或裸定时任务。
 */
class ArchitectureGuardTest {

    private static final Path ROOT = resolveRepositoryRoot();

    /**
     * common 不能反向依赖商品/库存领域实现模块。
     */
    @Test
    void commonModuleShouldNotDependOnDomainImplementations() throws Exception {
        String pom = Files.readString(ROOT.resolve("waynboot-common/pom.xml"));

        assertThat(pom)
                .doesNotContain("<artifactId>waynboot-domain-inventory</artifactId>")
                .doesNotContain("<artifactId>waynboot-domain-goods</artifactId>");
    }

    /**
     * promotion 领域模块不能依赖 common，避免领域实现反向耦合通用大包。
     */
    @Test
    void promotionModuleShouldNotDependOnCommon() throws Exception {
        String pom = Files.readString(ROOT.resolve("waynboot-domain-promotion/pom.xml"));

        assertThat(pom).doesNotContain("<artifactId>waynboot-common</artifactId>");
    }

    /**
     * domain-api 只保留契约和模型，不能继续暴露支付渠道 SDK 依赖。
     */
    @Test
    void domainApiShouldNotExposePaymentSdkDependencies() throws Exception {
        String pom = Files.readString(ROOT.resolve("waynboot-domain-api/pom.xml"));
        String sourceText = readAllJavaSources(ROOT.resolve("waynboot-domain-api/src/main/java"));

        assertThat(pom)
                .doesNotContain("<artifactId>weixin-java-pay</artifactId>")
                .doesNotContain("<artifactId>alipay-sdk-java</artifactId>");
        assertThat(sourceText)
                .doesNotContain("com.github.binarywang")
                .doesNotContain("com.alipay.api")
                .doesNotContain("WxPayException")
                .doesNotContain("AlipayApiException")
                .doesNotContain("WxPayUnifiedOrderV3Result");
    }

    /**
     * 交易治理定时任务必须带分布式锁，避免 admin 多实例重复执行对账和库存快照任务。
     */
    @Test
    void tradeGovernanceSchedulesShouldHaveDistributedLocks() throws Exception {
        Class<?> taskClass = Class.forName("com.wayn.admin.api.schedule.TradeGovernanceScheduledTask");
        List<Method> scheduledMethods = List.of(taskClass.getDeclaredMethods()).stream()
                .filter(method -> method.isAnnotationPresent(Scheduled.class))
                .toList();

        assertThat(scheduledMethods).isNotEmpty();
        assertThat(scheduledMethods)
                .allSatisfy(method -> assertThat(hasSchedulerLock(method))
                        .as(method.getName() + " should use ShedLock")
                        .isTrue());
    }

    /**
     * 判断方法是否声明 ShedLock 注解。
     *
     * @param method 待检查方法
     * @return true=已声明 SchedulerLock
     */
    private boolean hasSchedulerLock(Method method) {
        return List.of(method.getAnnotations()).stream()
                .anyMatch(annotation -> "net.javacrumbs.shedlock.spring.annotation.SchedulerLock"
                        .equals(annotation.annotationType().getName()));
    }

    /**
     * 读取指定目录下所有 Java 源码。
     * 结构测试需要检查整个契约模块，避免三方 SDK 类型从非 Service 文件重新泄漏。
     *
     * @param sourceRoot Java 源码根目录
     * @return 拼接后的源码文本
     */
    private String readAllJavaSources(Path sourceRoot) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                builder.append(Files.readString(path)).append('\n');
            }
        }
        return builder.toString();
    }

    /**
     * 解析仓库根目录。
     * Maven 在子模块执行测试时工作目录可能是模块目录，因此需要向上查找根 POM。
     *
     * @return 仓库根目录
     */
    private static Path resolveRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("waynboot-common/pom.xml"))
                    && Files.exists(current.resolve("waynboot-domain-api/pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位仓库根目录");
    }
}
