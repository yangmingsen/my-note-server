package top.yms.note.vo;

public class NoteSearchResult implements SearchResult{
    private int resType;
    private String result;
    private Long id;
    private Long parentId;
    private String type;
    private String isFile;
    private String encrypted;

    @Override
    public int getResType() {
        return resType;
    }

    public void setResType(int resType) {
        this.resType = resType;
    }

    @Override
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public Long getId() {
        return id;
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

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    @Override
    public String toString() {
        return "NoteSearchResult{" +
                "resType=" + resType +
                ", result='" + result + '\'' +
                ", id=" + id +
                ", parentId=" + parentId +
                ", type='" + type + '\'' +
                ", isFile='" + isFile + '\'' +
                '}';
    }
}
