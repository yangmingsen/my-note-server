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

    public static final String NOTE_META_FILE_RELATION_KEY = NOTE_META_PREFIX+"file::relation::";

    /**
     * system config cackeKey
     */
    public static final String SYSCFG_KEY = NOTE_PREFIX+"sys::cfg::";

    /**
     * 保存已爬取或待爬取的集合
     */
    public static final String CRAWLER_DUP_SET = NOTE_PREFIX+"url::set::";

    /**
     * 已经抓取到的网络文章集合
     */
    public static final String CRAWLER_SUCCESS_SET = NOTE_PREFIX+"url::success::set::";

    /**
     * 抓取失败的url集合
     */
    public static final String CRAWLER_FAIL_SET = NOTE_PREFIX+"url::fail::set::";

    /**
     * 黑名单url
     */
    public static final String CRAWLER_BLACKLIST_SET = NOTE_PREFIX+"url::blacklist::set::";

    /**
     * 待入队队列集合
     */
    public static final String CRAWLER_WAIT_ENQUEUE_SET = NOTE_PREFIX+"url::wait-enqueue::set::";

    /**
     * 空数据集合
     * 抓取后，发现是空数据
     */
    public static final String CRAWLER_EMPTY_DATA_SET = NOTE_PREFIX+"url::empty-data::set::";


    /**
     * 异步上传文件list
     */
    public static final String ASYNC_UPLOAD_FILE_LIST = NOTE_PREFIX+"async::list::upload-file::";

    /**
     * 异步获取文件失败的列表
     */
    public static final String ASYNC_UPLOAD_FILE_FAIL_LIST = NOTE_PREFIX+"async::list::upload-file-fail::";






}
