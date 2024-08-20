package top.yms.note.conpont;

import top.yms.note.dto.NoteSearchDto;
import top.yms.note.vo.SearchResult;

import java.util.List;

public interface NoteSearch {
    List<SearchResult> doSearch(NoteSearchDto noteSearchDto);
}
