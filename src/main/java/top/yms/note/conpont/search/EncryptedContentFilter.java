package top.yms.note.conpont.search;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.INoteCache;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.entity.NoteIndex;
import top.yms.note.service.NoteIndexService;
import top.yms.note.vo.SearchResult;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class EncryptedContentFilter implements SensitiveContentFilter{

    @Qualifier(NoteConstants.weakMemoryNoteCache)
    @Resource
    private NoteCacheService noteCacheService;

    @Resource
    private NoteIndexService noteIndexService;

    @Override
    public List<SearchResult> filter(List<SearchResult> searchResults) {
        List<SearchResult> filterRes = new LinkedList<>();
        for (SearchResult searchResult : searchResults) {
            if (!hit(searchResult)) {
                filterRes.add(searchResult);
            }
        }
        return filterRes;
    }

    /**
     * 是否击中
     * @param searchResult
     * @return true 击中
     */
    private boolean hit(SearchResult searchResult) {
        Long id = searchResult.getId();
        Object res = noteCacheService.find(id.toString());
        if (res != null) {
            return NoteConstants.ENCRYPTED_FLAG.equals(res);
        }
        //再过数据库
        NoteIndex noteIndex = noteIndexService.findOne(id);
        //自己是加密情况
        if (NoteConstants.ENCRYPTED_FLAG.equals(noteIndex.getEncrypted())) {
            noteCacheService.add(id.toString(), NoteConstants.ENCRYPTED_FLAG);
            return true;
        }
        //父级目录有加密情况 //最大循环
        for (int i=0; i<20; i++) {
            Long parentId = noteIndex.getParentId();
            if (NoteConstants.ROOT_DIR_FLAG.equals(parentId.toString())) {
                //应该不会存在加密自己的root目录，so暂时不管这个root目录加密
                break;
            }
            noteIndex = noteIndexService.findOne(parentId);
            if (NoteConstants.ENCRYPTED_FLAG.equals(noteIndex.getEncrypted())) {
                noteCacheService.add(id.toString(), NoteConstants.ENCRYPTED_FLAG);
                return true;
            }
        }
        noteCacheService.add(id.toString(), NoteConstants.ENCRYPTED_UN_FLAG);
        return false;
    }


}
