package top.yms.note.conpont;

import top.yms.note.dto.NoteSearchDto;
import top.yms.note.vo.SearchResult;

import java.util.List;

/**
 * 全文搜索服务
 */
public interface NoteSearchService {
    List<SearchResult> doSearch(NoteSearchDto noteSearchDto);
}
