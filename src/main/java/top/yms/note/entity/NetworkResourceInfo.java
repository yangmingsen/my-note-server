package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_network_resource_info
 */
public class NetworkResourceInfo {
    /**
     */
    private String noteFileId;

    /**
     * 类型
     */
    private String suffix;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源地址
     */
    private String url;

    /**
     * create time
     */
    private Date createTime;

    public String getNoteFileId() {
        return noteFileId;
    }

    public void setNoteFileId(String noteFileId) {
        this.noteFileId = noteFileId == null ? null : noteFileId.trim();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? null : suffix.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getFullName() {
        return name+"."+suffix;
    }
}