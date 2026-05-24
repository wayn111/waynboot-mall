package com.wayn.domain.trade.outbox;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 本地消息错误信息格式化工具。
 * 统一处理补偿日志和本地消息表中的异常摘要，避免多字节字符被截断后出现乱码。
 */
final class LocalMessageErrorFormatter {

    private LocalMessageErrorFormatter() {
    }

    /**
     * 按 UTF-8 字节上限安全截断错误信息。
     *
     * @param errorMessage 原始错误信息
     * @param maxBytes 最大字节数
     * @return 不会截断半个 Unicode 字符的错误摘要
     */
    static String truncateUtf8(String errorMessage, int maxBytes) {
        if (StringUtils.isBlank(errorMessage) || maxBytes <= 0) {
            return "";
        }
        if (utf8Length(errorMessage) <= maxBytes) {
            return errorMessage;
        }
        return truncateByCodePoint(errorMessage, maxBytes);
    }

    /**
     * 按 Unicode code point 逐字符累加字节数。
     *
     * @param value 原始字符串
     * @param maxBytes 最大字节数
     * @return 截断后的字符串
     */
    private static String truncateByCodePoint(String value, int maxBytes) {
        StringBuilder builder = new StringBuilder();
        int usedBytes = 0;
        for (int offset = 0; offset < value.length(); ) {
            int codePoint = value.codePointAt(offset);
            String character = new String(Character.toChars(codePoint));
            int characterBytes = utf8Length(character);
            if (usedBytes + characterBytes > maxBytes) {
                break;
            }
            builder.append(character);
            usedBytes += characterBytes;
            offset += Character.charCount(codePoint);
        }
        return builder.toString();
    }

    /**
     * 计算字符串的 UTF-8 字节数。
     *
     * @param value 原始字符串
     * @return UTF-8 字节数
     */
    private static int utf8Length(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
