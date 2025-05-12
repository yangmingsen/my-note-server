package top.yms.note.utils;

public class FNVHash {

    public static long fnv1aHash64(String content) {
        final long FNV_64_PRIME = 1099511628211L;
        final long FNV_64_OFFSET_BASIS = 0xcbf29ce484222325L; // 用16进制避免超限问题

        long hash = FNV_64_OFFSET_BASIS;
        for (int i = 0; i < content.length(); i++) {
            hash ^= content.charAt(i);
            hash *= FNV_64_PRIME;
        }
        return hash;
    }
}

