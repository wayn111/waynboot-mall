package com.wayn.common.core.service.message;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalMessageErrorFormatterTest {

    /**
     * 中文错误信息按 UTF-8 字节上限截断时不能切断半个中文字符。
     */
    @Test
    void truncateUtf8DoesNotBreakChineseCharacters() {
        String longChineseError = "错误".repeat(600);

        String truncatedError = LocalMessageErrorFormatter.truncateUtf8(longChineseError, 1000);

        assertEquals(999, truncatedError.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(333, truncatedError.length());
    }

    /**
     * 未超过字节上限的 ASCII 错误信息应原样返回。
     */
    @Test
    void truncateUtf8ReturnsOriginalWhenWithinLimit() {
        String errorMessage = "rabbit down";

        String truncatedError = LocalMessageErrorFormatter.truncateUtf8(errorMessage, 100);

        assertEquals(errorMessage, truncatedError);
    }

    /**
     * 空错误信息或非法字节上限统一返回空字符串，避免写入 null 或无意义摘要。
     */
    @Test
    void truncateUtf8ReturnsEmptyWhenInputIsBlankOrLimitInvalid() {
        assertEquals("", LocalMessageErrorFormatter.truncateUtf8(null, 100));
        assertEquals("", LocalMessageErrorFormatter.truncateUtf8("rabbit down", 0));
    }
}
