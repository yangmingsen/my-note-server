package top.yms.note.dto;

public class NoteSearchDto {
    private Long userId;
    private String keyword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return "NoteSearchDto{" +
                "userId=" + userId +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
