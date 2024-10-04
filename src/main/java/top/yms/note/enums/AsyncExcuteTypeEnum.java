package top.yms.note.enums;

/**
 * 任务执行方式
 */
public enum AsyncExcuteTypeEnum {
    SYNC_TASK("立即执行任务", "sync_task"),
    CALLER_TASK("使用调用者线程执行的任务", "caller_task"),
    TIMED_TASK("定时任务", "timed_task");

    private final String name;
    private final String value;


    AsyncExcuteTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static AsyncExcuteTypeEnum apply(String v) {
        if (v == null) return null;
        for (AsyncExcuteTypeEnum nt : AsyncExcuteTypeEnum.values()) {
            if (nt.value.equals(v)) {
                return nt;
            }
        }
        return null;
    }
}
