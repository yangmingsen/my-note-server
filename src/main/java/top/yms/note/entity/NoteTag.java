package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_tag
 */
public class NoteTag {
    /**
     */
    private Long id;

    /**
     * user id
     */
    private Long userId;

    /**
     * tag name
     */
    private String tagName;

    /**
     */
    private Date createTime;

    /**
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName == null ? null : tagName.trim();
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
}