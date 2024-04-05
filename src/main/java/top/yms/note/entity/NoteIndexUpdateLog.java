package top.yms.note.entity;

import top.yms.note.config.NoteOpType;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_index_update_log
 */
public class NoteIndexUpdateLog {
    /**
     */
    private Long id;

    /**
     */
    private Long indexId;

    /**
     * 类型(add,del,upd)
     */
    private String type;

    /**
     */
    private Date createTime;

    /**
     * 元数据修改记录
     */
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getType() {
        return type;
    }

    public void setType(NoteOpType type) {
        this.type = type == null ? null : type.getName().trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}