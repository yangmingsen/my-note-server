package top.yms.note.entity;

import top.yms.note.dto.INoteData;

import java.util.Date;
import java.util.List;

/**
 *
 * create by yangmingsen
 * t_note_data
 */
public class NoteData implements INoteData {
    /**
     */
    private Long id;

    /**
     */
    private Long userId;

    /**
     * 笔记内容
     */
    private String content;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
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

    @Override
    public String toString() {
        return "NoteData{" +
                "id=" + id +
                ", userId=" + userId +
                '}';
    }

    @Override
    public NoteMeta getNoteIndex() {
        return null;
    }

    @Override
    public NoteData getNoteData() {
        return null;
    }

    @Override
    public List<NoteDataVersion> getNoteDataVersionList() {
        return null;
    }
}