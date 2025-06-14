package top.yms.note.vo;

import top.yms.note.entity.NoteMeta;

/**
 * Created by yangmingsen on 2024/10/4.
 */
public class NoteMetaExtVo extends NoteMeta {
    private String tmpToken;

    public String getTmpToken() {
        return tmpToken;
    }

    public void setTmpToken(String tmpToken) {
        this.tmpToken = tmpToken;
    }
}
