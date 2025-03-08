package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/8/13.
 */
public class NoteSearchCondition {
    private String searchContent;

    public String getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    @Override
    public String toString() {
        return "NoteSearchCondition{" +
                "searchContent='" + searchContent + '\'' +
                '}';
    }
}
