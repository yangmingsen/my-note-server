package top.yms.note.vo;

import java.util.Collections;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/13.
 */
public class NoteSearchVo {
    private List<SearchResult> listResult;

    public NoteSearchVo(List<SearchResult> listResult) {
        this.listResult = listResult;
    }
    public NoteSearchVo(){}

    public List<SearchResult> getListResult() {
        return listResult;
    }

    public void setListResult(List<SearchResult> listResult) {
        this.listResult = listResult;
    }

    public static NoteSearchVo getEmpty() {
        NoteSearchVo res = new NoteSearchVo();
        res.setListResult(Collections.emptyList());
        return res;
    }
}
