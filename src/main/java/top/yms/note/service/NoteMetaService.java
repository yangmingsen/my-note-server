package top.yms.note.service;

import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.NoteMoveDto;
import top.yms.note.dto.NoteSearchCondition;
import top.yms.note.entity.AntTreeNode;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteTree;
import top.yms.note.vo.NoteInfoVo;
import top.yms.note.vo.NoteSearchVo;

import java.util.List;

public interface NoteMetaService {
    /**
     * 找到一个NoteIndex
     * @param id id
     * @return noteIndex
     */
    NoteMeta findOne(Long id);

    NoteSearchVo findNoteByCondition(NoteSearchCondition searchDto);

    List<NoteMeta> findByUserId(Long userid);

    NoteMeta findBySiteId(String siteId);

    List<NoteMeta> findNoteMetaList(Long parentId, Long uid);

    NoteMeta findRoot();

    List<NoteMeta> findBackParentDir(Long id);

    NoteTree findCurUserRootNoteTree();

    List<NoteTree> findNoteTreeByUid(Long uid);

    List<AntTreeNode> findAntTreeExcludeEncrypted(Long userId);

    AntTreeNode transferToAntTree(NoteTree noteTree);

    void add(NoteMeta note);

    void update(NoteMeta note);

    void destroyNote(Long id);

    void delNote(Long id);

    void delDir(Long parentId);

    List<NoteMeta> findBy(NoteIndexQuery query);

    void findBreadcrumb(Long id, List<NoteMeta> list);

    String findBreadcrumbForSearch(Long noteId);

    NoteInfoVo getNoteAndSite(Long id);

    List<NoteMeta> getRecentFiles();

    List<NoteMeta> getDeletedFiles();

    int allDestroy();

    int allRecover();

    void updateMove(NoteMoveDto noteMoveDto);

    void encryptedReadNote(Long id);

    void unEncryptedReadNote(Long id);

    List<NoteMeta> findRecentVisitList();

    /**
     * 自动处理标记加密笔记（处理旧版本只对noteIndex做标记，没有做实际笔记加密处理)
     */
    @Deprecated//不再使用
    void autoScanEncrypt();

    /**
     * 自动取消加密标记笔记
     */
    void autoDecryptedAllNote();

    Long createParentDir(String dirName, Long parentId);
}
