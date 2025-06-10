package top.yms.note.conpont.note;

import top.yms.note.dto.req.NoteShareReqDto;
import top.yms.note.entity.NoteShareInfo;
import top.yms.note.vo.NoteShareVo;

public interface NoteShare {

    /**
     * 是否支持笔记分享
     * @param noteType true-支持，false-不支持
     * @return true/false
     */
    boolean supportShare(String noteType);

    /**
     * 获取分享note
     * @param noteId noteId
     * @return noteShareVo
     */
    NoteShareVo shareNoteGet(NoteShareReqDto noteShareReqDto);

    /**
     * 关闭笔记分享
     * @param noteId noteId
     */
    void shareNoteClose(NoteShareReqDto noteShareReqDto);

    /**
     * 开启笔记分享
     * @param noteId noteId
     * @return NoteShareInfo
     */
    NoteShareInfo shareNoteOpen(NoteShareReqDto noteShareReqDto);

}
