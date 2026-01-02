package top.yms.note.comm;

public abstract class NoteCacheKey {
    /**
     * 分享资源key
     */
    public static final String SERVER_SHARE_RESOURCE_KEY = "SERVER_SHARE_RESOURCE_KEY";

    public static final String VIEW_SHARE_KEY = "VIEW_SHARE_KEY";

    /**
     * DirSizeComputeTask cache key
     */
    public static final String DIR_SIZE_COMPUTE_TASK_KEY = "DSCTK:";


    protected static final String NOTE_PREFIX = "note::";

    /**
     * NoteMeta 前缀
     */
    protected static final String NOTE_META_PREFIX = NOTE_PREFIX+"meta::";

    /**
     * noteData 前缀
     */
    protected static final String NOTE_DATA_PREFIX = NOTE_PREFIX+"data::";

    public static final String NOTE_META_KEY = NOTE_META_PREFIX+"key::";

    /**
     * when update: 当出现用户配置更新时
     */
    public static final String NOTE_USER_CONFIG_KEY = NOTE_PREFIX+"user:config::";

    /**
     * when update: 当出现目录更新时
     */
    public static final String NOTE_META_TREE_KEY = NOTE_META_PREFIX+"tree::";

    /**
     * when update: 注意删除某个目录时，需要删除删除此缓存
     */
    public static final String NOTE_META_BREADCRUMB_KEY = NOTE_META_PREFIX+"breadcrumb::";

    /**
     * when update: 应该要在某个目录（Aa）下新增文件或文件夹时，删除当前Aa的缓存
     */
    public static final String NOTE_META_SUB_KEY = NOTE_META_PREFIX+"sub::";

    /**
     * when update: 应该在修改元数据时
     */
    public static final String NOTE_META_LIST_KEY = NOTE_META_PREFIX+"list";

    /**
     * when update: 目录更新时（A作为父目录，B,C作为A子文件，当A里文件列表发生变化，就需要更新）
     */
    public static final String NOTE_META_PARENT_LIST_KEY = NOTE_META_PREFIX+"parent-list";

    /**
     * when update: 当出现noteData数据更新时
     */
    public static final String NOTE_DATA_LIST_KEY = NOTE_DATA_PREFIX+"list";





}
