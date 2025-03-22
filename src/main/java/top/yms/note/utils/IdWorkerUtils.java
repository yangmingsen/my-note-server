package top.yms.note.utils;

import top.yms.note.config.SpringContext;

public class IdWorkerUtils {
    public static long getId() {
       return SpringContext.getBean(IdWorker.class).nextId();
    }
}
