package top.yms.note.conpont;

import top.yms.note.entity.NoteShareInfo;
import top.yms.note.vo.NoteShareVo;

/**
 * 分享服务
 */
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

    /**
     * 获取分享资源
     * @param id 资源id
     * @return 资源文件
     */
    AnyFile shareResource(String id);
}
