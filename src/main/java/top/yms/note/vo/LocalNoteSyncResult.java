package top.yms.note.vo;

/**
 * Created by yangmingsen on 2024/10/13.
 */
public class LocalNoteSyncResult {
    private Long id;
    private boolean isFile;
    private int type;

    public LocalNoteSyncResult() {

    }

    public LocalNoteSyncResult(Long id, boolean isFile) {
        this.id = id;
        this.isFile = isFile;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }
}
