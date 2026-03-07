package top.yms.note.service;

import top.yms.note.entity.FileStoreRelation;

public interface FileStoreRelationService {

    /**
     * 更新
     * @param fileStoreRelation
     */
    void update(FileStoreRelation fileStoreRelation);

    /**
     * 通过noteFileId获取映射信息
     * @param noteFileId
     * @return
     */
    FileStoreRelation findOneByNoteFileId(String noteFileId);
}
