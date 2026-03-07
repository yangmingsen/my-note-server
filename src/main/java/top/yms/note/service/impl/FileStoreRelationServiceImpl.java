package top.yms.note.service.impl;

import org.springframework.stereotype.Service;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.mapper.FileStoreRelationMapper;
import top.yms.note.service.FileStoreRelationService;

import javax.annotation.Resource;

@Service
public class FileStoreRelationServiceImpl implements FileStoreRelationService {

    @Resource
    private FileStoreRelationMapper fileStoreRelationMapper;

    @Override
    public void update(FileStoreRelation fileStoreRelation) {

    }

    @Override
    public FileStoreRelation findOneByNoteFileId(String noteFileId) {
        return fileStoreRelationMapper.selectByNoteFileId(noteFileId);
    }
}
