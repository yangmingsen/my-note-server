package top.yms.note.vo;

import top.yms.note.entity.NoteIndex;

/**
 * Created by yangmingsen on 2024/10/4.
 */
public class NoteIndexVo extends NoteIndex {
    private String tmpToken;

    public String getTmpToken() {
        return tmpToken;
    }

    public void setTmpToken(String tmpToken) {
        this.tmpToken = tmpToken;
    }
}
