package top.yms.note.enums;

public enum AsyncTaskEnum {
    SYNC_USER_CONFIG("同步用户配置", "sync_user_config"),
    SYNC_Note_Index_UPDATE("同步某个笔记全文搜索索引", "sync_note_index_update"),
    SYNC_COMPUTE_RECENT_VISIT("同步计算用户最近访问信息", "sync_compute_recent_visit"),
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
