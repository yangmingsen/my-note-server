package top.yms.note.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_index
 */
public class NoteIndex {
    /**
     */
    private Long id;

    /**
     */
    private Long parentId;

    /**
     */
    private Long userId;

    /**
     */
    private String name;

    /**
     * 是否为目录(0是目录，1是文件)
     */
    private String isFile;

    /**
     * 文件类型(为文件时有值)
     */
    private String type;

    /**
     * 是否删除(1删除，0否)
     */
    private String del;

    /**
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     */
    private Date updateTime;

    /**
     * 存储位置(mysql,mongo)
     */
    private String storeSite;

    /**
     * 可能是t_note_file表的t_file_id
     */
    private String siteId;

    /**
     * 笔记大小
     */
    private Long size;

    /**
     * 是否加密访问 0 不需要, 1需要
     */
    private String encrypted;

    private Date viewTime;

    public Date getViewTime() {
        return viewTime;
    }

    public void setViewTime(Date viewTime) {
        this.viewTime = viewTime;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getStoreSite() {
        return storeSite;
    }

    public void setStoreSite(String storeSite) {
        this.storeSite = storeSite;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getIsFile() {
        return isFile;
    }

    public void setIsFile(String isFile) {
        this.isFile = isFile == null ? null : isFile.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getDel() {
        return del;
    }

    public void setDel(String del) {
        this.del = del == null ? null : del.trim();
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


    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "NoteIndex{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", isFile='" + isFile + '\'' +
                ", type='" + type + '\'' +
                ", del='" + del + '\'' +
                ", size='" + size + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}