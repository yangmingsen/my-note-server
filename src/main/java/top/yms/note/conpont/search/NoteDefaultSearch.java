package top.yms.note.conpont.search;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteSearchService;
import top.yms.note.dto.NoteSearchDto;
import top.yms.note.entity.SearchLog;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.service.impl.NoteSearchLogServiceImpl;
import top.yms.note.utils.IdWorker;
import top.yms.note.vo.NoteIndexSearchResult;
import top.yms.note.vo.SearchResult;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component(NoteConstants.noteDefaultSearch)
public class NoteDefaultSearch implements NoteSearchService {

    @Resource
    NoteSearchLogServiceImpl noteSearchLogServiceImpl;

    @Resource
    IdWorker idWorker;

    @Resource
    NoteIndexMapper noteIndexMapper;

    @Override
    public List<SearchResult> doSearch(NoteSearchDto noteSearchDto) {
        final String keyword = noteSearchDto.getKeyword();
        Long uid = noteSearchDto.getUserId();
        //add search log
        SearchLog searchLog = new SearchLog();
        searchLog.setId(idWorker.nextId());
        searchLog.setSearchContent(noteSearchDto.getKeyword());
        searchLog.setCreateTime(new Date());
        searchLog.setUserId(uid);
        noteSearchLogServiceImpl.add(searchLog);
        return noteIndexMapper.searchName(keyword, uid)
                .stream()
                .map(noteIndex -> {
                    NoteIndexSearchResult searchResult = new NoteIndexSearchResult();
                    String highlightResult =
                            noteIndex.getName().replaceAll(keyword,
                                    "<span style=\"color: red\">"+keyword+"</span>");
                    searchResult.setResult(highlightResult);
                    searchResult.setId(noteIndex.getId());
                    searchResult.setParentId(noteIndex.getParentId());
                    searchResult.setIsFile(noteIndex.getIsFile());
                    searchResult.setType(noteIndex.getType());
                    return (SearchResult)searchResult;
                })
                .collect(Collectors.toList());
    }
}
