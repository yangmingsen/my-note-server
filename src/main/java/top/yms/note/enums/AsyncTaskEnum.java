package top.yms.note.enums;

public enum AsyncTaskEnum {
    SYNC_USER_CONFIG("同步用户配置", "sync_user_config"),
    SYNC_Note_Index_UPDATE("同步某个笔记全文搜索索引", "sync_note_index_update"),
    SYNC_COMPUTE_RECENT_VISIT("同步计算用户最近访问信息", "sync_compute_recent_visit"),
    BOOKMARKS_SYNC_TASK("同步bookmarks", "bookmarks_sync_task"),
    NOTE_CONTENT_VERSION_OPTIMIZE("笔记版本优化", "note_content_version_optimize"),
    NOTE_DIR_SIZE_COMPUTE_TASK("目录大小计算", "NOTE_DIR_SIZE_COMPUTE_TASK"),
    ;

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
