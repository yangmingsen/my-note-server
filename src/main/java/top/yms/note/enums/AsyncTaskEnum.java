package top.yms.note.enums;

public enum AsyncTaskEnum {
    ASYNC_USER_CONFIG("同步用户配置", "async_user_config"),
    RELOAD_NOTE_DATA_VERSION("重新计算笔记版本", "reload_note_data_version");

    private final String name;
    private final String value;

    AsyncTaskEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static AsyncTaskEnum apply(String v) {
        if (v == null) return null;
        for (AsyncTaskEnum nt : AsyncTaskEnum.values()) {
            if (nt.value.equals(v)) {
                return nt;
            }
        }
        return null;
    }
}
