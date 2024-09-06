package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/9/6.
 */
public class NoteMoveDto {
    private Long fromId;
    private Long toId;

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    @Override
    public String toString() {
        return "NoteMoveDto{" +
                "fromId=" + fromId +
                ", toId=" + toId +
                '}';
    }
}
