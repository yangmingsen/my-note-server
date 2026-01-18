package top.yms.note.service;

import top.yms.note.dto.INoteData;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;

import java.util.List;

public interface NoteDataService {

    /**
     * 查询笔记content版本
     * @param noteId
     * @return
     */
    List<NoteDataVersion> findDataVersionList(Long noteId);

    /**
     * 删除数据版本
     * @param id
     */
    void deleteDataVersion(Long id);

    /**
     * 增加笔记内容数据
     * @param iNoteData
     */
    void save(INoteData iNoteData);

    /**
     * 查询笔记数据应该使用此方法
     * @param id
     * @return
     */
    NoteData findNoteData(Long id);

    /**
     * 查询， 此方法仅供 NoteStoreService 使用
     * @param id
     * @return
     */
    NoteData findOneByPk(Long id);

    /**
     * 直接更新数据使用此方法， 仅供系统内部使用，业务更新应该使用save方法。
     * @param noteData data
     */
    void update(NoteData noteData);

}
