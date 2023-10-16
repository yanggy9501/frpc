package com.freeing.rpc.common.utils;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * 序列化时针对消息头序列化类型的操作
 *
 * @author yanggy
 */
public class SerializationUtils {

    private static final String PADDING_STRING = "0";

    /**
     * 约定序列化类型最大长度为16
     */
    public static final int MAX_SERIALIZATION_TYPE_COUNT = 16;

    /**
     * 为长度不足16的字符串后面补0
     *
     * @param str 原始字符串
     * @return 补0后的字符串
     */
    public static String paddingString(String str) {
        str = nullToEmpty(str);
        if (str.length() >= MAX_SERIALIZATION_TYPE_COUNT) {
            return str;
        }
        int paddingCount = MAX_SERIALIZATION_TYPE_COUNT - str.length();
        StringBuilder paddingStr = new StringBuilder(str);
        IntStream.range(0, paddingCount).forEach(i -> paddingStr.append(PADDING_STRING));
        return paddingStr.toString();
    }

    public static String subString(String str) {
        str = nullToEmpty(str);
        return str.replace(PADDING_STRING, "");
    }

    public static String nullToEmpty(String str) {
        return Objects.isNull(str) ? "" : str;
    }
}
