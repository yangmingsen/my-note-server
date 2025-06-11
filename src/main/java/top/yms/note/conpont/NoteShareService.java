package top.yms.note.conpont;

import top.yms.note.entity.NoteShareInfo;
import top.yms.note.vo.NoteShareVo;

public interface NoteShareService {
    /**
     * 获取分享note
     * @param noteId
     * @return
     */
    NoteShareVo shareNoteGet(Long noteId);

    /**
     * 关闭笔记分享
     * @param noteId
     */
    void shareNoteClose(Long noteId);

    /**
     * 开启笔记分享
     * @param noteId
     * @return
     */
    NoteShareInfo shareNoteOpen(Long noteId);
}
