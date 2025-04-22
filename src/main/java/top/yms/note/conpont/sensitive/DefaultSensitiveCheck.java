package top.yms.note.conpont.sensitive;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.conpont.SensitiveService;
import top.yms.note.entity.NoteIndex;
import top.yms.note.service.NoteIndexService;

import javax.annotation.Resource;

/**
 * 加密内容检查命中
 */
@Component
public class DefaultSensitiveCheck implements SensitiveService {
    @Qualifier(NoteConstants.weakMemoryNoteCache)
    @Resource
    private NoteCacheService noteCacheService;

    @Resource
    private NoteIndexService noteIndexService;

    @Override
    public boolean isSensitive(Long id) {
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
