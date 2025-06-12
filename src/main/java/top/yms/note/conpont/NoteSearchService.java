package top.yms.note.conpont;

import top.yms.note.dto.NoteSearchDto;
import top.yms.note.vo.SearchResult;

import java.util.List;

/**
 * 全文搜索服务
 */
public interface NoteSearchService {
    /**
     * 全文搜索服务统一接口
     * <p>目前实现基于关键字，可标题和文体全方位搜</p>
     * @param noteSearchDto 搜索条件
     * @return 搜索结果
     */
    List<SearchResult> doSearch(NoteSearchDto noteSearchDto);
}
