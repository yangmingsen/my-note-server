package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/9/21.
 */
public class NoteDataDto implements INoteData{
    private Long id;
    private Long userId;

    /**
     * 笔记内容
     */
    private String content;

    private String textContent;

    /**
     * 类型：wer,md,mindmap
     */
    private String type;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "NoteDataDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", textContent='" + textContent + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
