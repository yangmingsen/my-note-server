package top.yms.note.entity;

/**
 *
 * create by yangmingsen
 * t_note_data
 */
public class NoteData {
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
}