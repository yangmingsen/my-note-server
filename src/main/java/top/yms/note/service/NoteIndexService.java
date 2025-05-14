package top.yms.note.service;

import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.NoteMoveDto;
import top.yms.note.dto.NoteSearchCondition;
import top.yms.note.entity.AntTreeNode;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteTree;
import top.yms.note.vo.NoteInfoVo;
import top.yms.note.vo.NoteSearchVo;

import java.util.List;

public interface NoteIndexService {
    /**
     * 找到一个NoteIndex
     * @param id id
     * @return noteIndex
     */
    NoteIndex findOne(Long id);

    NoteSearchVo findNoteByCondition(NoteSearchCondition searchDto);

    List<NoteIndex> findByUserId(Long userid);

    NoteIndex findBySiteId(String siteId);

    List<NoteIndex> findSubBy(Long parentId, Long uid);

    NoteIndex findRoot();

    List<NoteIndex> findBackParentDir(Long id);

    NoteTree findCurUserRootNoteTree();

    List<NoteTree> findNoteTreeByUid(Long uid);

    List<AntTreeNode> findAntTreeExcludeEncrypted(Long userId);

    AntTreeNode transferToAntTree(NoteTree noteTree);

    void add(NoteIndex note);

    void update(NoteIndex note);

    void destroyNote(Long id);

    void delNote(Long id);

    void delDir(Long parentId);

    List<NoteIndex> findBy(NoteIndexQuery query);

    void findBreadcrumb(Long id, List<NoteIndex> list);

    String findBreadcrumbForSearch(Long noteId);

    NoteInfoVo getNoteAndSite(Long id);

    List<NoteIndex> getRecentFiles();

    List<NoteIndex> getDeletedFiles();

    int allDestroy();

    int allRecover();

    void updateMove(NoteMoveDto noteMoveDto);

    void encryptedReadNote(Long id);

    void unEncryptedReadNote(Long id);

    List<NoteIndex> findRecentVisitList();

    /**
     * 自动处理标记加密笔记（处理旧版本只对noteIndex做标记，没有做实际笔记加密处理)
     */
    @Deprecated//不再使用
    void autoScanEncrypt();

    /**
     * 自动取消加密标记笔记
     */
    void autoDecryptedAllNote();
}
