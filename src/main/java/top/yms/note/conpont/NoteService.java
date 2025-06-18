package top.yms.note.conpont;

import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;

public interface NoteService extends NoteStoreService, NoteExportService, NoteEncryptService,
                                    NoteDestroyService, NoteShareService {
    /**
     * 获取当前笔记占用空间（B-字节)
     * @return size byte
     */
    default long getNoteSize() {throw new BusinessException(CommonErrorCode.E_200211);}

}
