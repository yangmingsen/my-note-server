package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_tag_user
 */
public class NoteTagUser {
    /**
     */
    private Long id;

    /**
     * 笔记id
     */
    private Long noteId;

    /**
     * 标签_id
     */
    private String tagId;

    /**
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId == null ? null : tagId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}