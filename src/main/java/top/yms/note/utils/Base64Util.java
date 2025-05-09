package top.yms.note.utils;

import java.util.Base64;

public class Base64Util {
    // 编码：将原始字符串转换为 Base64 编码字符串
    public static String encodeBase64Str(String original) {
        return Base64.getEncoder().encodeToString(original.getBytes());
    }

    // 解码：将 Base64 字符串解码为原始字符串
    public static String decodeStr(String base64Str) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        return new String(decodedBytes);
    }

}
