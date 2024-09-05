package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/9/5.
 */
public class NoteListQueryDto {
    /**
     * 0 创建时间
     * 1 更新时间
     * 2 文件名称
     * 3 文件大小
     */
    private int sortBy = 0;

    /**
     * 1 asc
     * 0 desc
     */
    private int asc = 1;

    /**
     * 父目录id.
     * 注意： 最近删除和最近文件不需要
     */
    private Long parentId;


    public int getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }

    public int getAsc() {
        return asc;
    }

    public void setAsc(int asc) {
        this.asc = asc;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "NoteListQueryDto{" +
                "sortBy=" + sortBy +
                ", asc=" + asc +
                ", parentId=" + parentId +
                '}';
    }
}
