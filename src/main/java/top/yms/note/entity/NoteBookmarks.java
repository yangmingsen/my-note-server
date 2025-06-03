package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_bookmarks
 */
public class NoteBookmarks {
    /**
     * 笔记id <=> t_note_index#id
     */
    private Long id;

    /**
     * 对应t_note_index#parent_id
     */
    private Long parentId;

    /**
     * 书签的名称（标题）:用户在书签管理器中看到的名称
     */
    private String name;

    /**
     * 书签类型:"url"表示普通网址书签
     */
    private String type;

    /**
     * 书签对应的网址:访问的具体网页地址
     */
    private String url;

    /**
     * 书签的内部ID:纯数字字符串，Chrome内部管理用
     */
    private Long chromeId;

    /**
     * 书签的全局唯一标识符:一个 UUID，用于唯一标识该书签
     */
    private String guid;

    /**
     * 书签添加时间:	Chrome 时间戳，单位是微秒级的Webkit时间，起点是1601年1月1日
     */
    private String dateAdded;

    /**
     * 书签最后一次被使用的时间:0 表示从未使用过
     */
    private String dateLastUsed;

    /**
     * 书签的元信息:这里包含额外的信息，比如最近一次访问时间等
     */
    private String metaInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 1已同步，0未同步
     */
    private String syncFlag;

    /**
     * 上传同步时间
     */
    private Date syncLastTime;

    /**
     * 用户id
     */
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Long getChromeId() {
        return chromeId;
    }

    public void setChromeId(Long chromeId) {
        this.chromeId = chromeId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid == null ? null : guid.trim();
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded == null ? null : dateAdded.trim();
    }

    public String getDateLastUsed() {
        return dateLastUsed;
    }

    public void setDateLastUsed(String dateLastUsed) {
        this.dateLastUsed = dateLastUsed == null ? null : dateLastUsed.trim();
    }

    public String getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(String metaInfo) {
        this.metaInfo = metaInfo == null ? null : metaInfo.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getSyncFlag() {
        return syncFlag;
    }

    public void setSyncFlag(String syncFlag) {
        this.syncFlag = syncFlag == null ? null : syncFlag.trim();
    }

    public Date getSyncLastTime() {
        return syncLastTime;
    }

    public void setSyncLastTime(Date syncLastTime) {
        this.syncLastTime = syncLastTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}