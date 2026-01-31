package top.yms.note.conpont.store;

public class AsyncFileSaveDto {

    /**
     * 待抓取url资源
     */
    private String fetchUrl;

    /**
     * 提前分配的noteFileId
     */
    private String noteFileId;

    private String suffix;

    private String tmpFileName;

    public String getTmpFileName() {
        return tmpFileName;
    }

    public void setTmpFileName(String tmpFileName) {
        this.tmpFileName = tmpFileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFetchUrl() {
        return fetchUrl;
    }

    public void setFetchUrl(String fetchUrl) {
        this.fetchUrl = fetchUrl;
    }

    public String getNoteFileId() {
        return noteFileId;
    }

    public void setNoteFileId(String noteFileId) {
        this.noteFileId = noteFileId;
    }
}
