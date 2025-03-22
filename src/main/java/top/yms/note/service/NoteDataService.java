package top.yms.note.service;

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

}
