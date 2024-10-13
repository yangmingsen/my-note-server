package top.yms.note.vo;

/**
 * Created by yangmingsen on 2024/8/13.
 */
public class NoteIndexSearchResult implements SearchResult {

    private int resType = SearchResult.Note_Index_Type;
    private String result;
    private Long id;
    private Long parentId;
    private String type;
    private String isFile;


    @Override
    public int getResType() {
        return resType;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getResult() {
        return result;
    }




    public void setResult(String result) {
        this.result = result;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setResType(int resType) {
        this.resType = resType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsFile() {
        return isFile;
    }

    public void setIsFile(String isFile) {
        this.isFile = isFile;
    }
}
