package top.yms.note.conpont.store;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.entity.FileStoreRelationExample;
import top.yms.note.mapper.FileStoreRelationMapper;
import top.yms.storage.client.StorageClient;

import top.yms.storage.entity.UploadResp;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Primary
@Component(NoteConstants.storageFileStoreService)
public class FileStorageService implements FileStoreService {

    @Qualifier(NoteConstants.mongoFileStoreService)
    @Resource
    private FileStoreService mongoFileStoreService;

    @Resource
    private StorageClient storageClient;

    @Resource
    private FileStoreRelationMapper fileStoreRelationMapper;

    private FileStoreRelationExample getQueryCondition(FileStoreRelation fileStoreRelation) {
        FileStoreRelationExample example = new FileStoreRelationExample();
        FileStoreRelationExample.Criteria criteria = example.createCriteria();
        if (fileStoreRelation.getId() != null) {
            criteria.andIdEqualTo(fileStoreRelation.getId());
        }
        if (StringUtils.isNotBlank(fileStoreRelation.getStorageFileId() )) {
            criteria.andStorageFileIdEqualTo(fileStoreRelation.getStorageFileId());
        }
        if (StringUtils.isNotBlank(fileStoreRelation.getMongoFileId() )) {
            criteria.andMongoFileIdEqualTo(fileStoreRelation.getMongoFileId());
        }
        return example;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 20)
    @Override
    public AnyFile loadFile(String id) {
        FileStoreRelation qry = new FileStoreRelation();
        qry.setMongoFileId(id);
        FileStoreRelationExample queryCondition = getQueryCondition(qry);
        List<FileStoreRelation> qryResp = fileStoreRelationMapper.selectByExample(queryCondition);
        if (qryResp.isEmpty()) {
            AnyFile anyFile = mongoFileStoreService.loadFile(id);
            UploadResp uploadRsp = storageClient.upload(anyFile.getInputStream(), anyFile.getFilename());
            FileStoreRelation fsr = new FileStoreRelation();
            fsr.setMongoFileId(id);
            fsr.setStorageFileId(uploadRsp.getFileId());
            fsr.setCreateTime(new Date());
            fileStoreRelationMapper.insertSelective(fsr);
            return new StorageFile(uploadRsp.getFileId(), storageClient);
        }
        return new StorageFile(qryResp.get(0).getStorageFileId(), storageClient);
    }


    @Override
    public String saveFile(MultipartFile file) throws Exception {
        UploadResp uploadResp = storageClient.upload(file.getInputStream(), file.getOriginalFilename());
        String id = mongoFileStoreService.saveFile(file);
        FileStoreRelation fsr = new FileStoreRelation();
        fsr.setMongoFileId(id);
        fsr.setStorageFileId(uploadResp.getFileId());
        fsr.setCreateTime(new Date());
        fileStoreRelationMapper.insertSelective(fsr);
        return id;
    }

    @Override
    public String saveFile(File file) throws Exception {
        UploadResp uploadResp = storageClient.upload(file);
        String id = mongoFileStoreService.saveFile(file);
        FileStoreRelation fsr = new FileStoreRelation();
        fsr.setMongoFileId(id);
        fsr.setStorageFileId(uploadResp.getFileId());
        fsr.setCreateTime(new Date());
        fileStoreRelationMapper.insertSelective(fsr);
        return id;
    }

    @Override
    public String saveFile(String localPath) throws Exception {
        return saveFile(new File(localPath));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 20)
    @Override
    public boolean delFile(String id) {
        FileStoreRelation qry = new FileStoreRelation();
        qry.setMongoFileId(id);
        FileStoreRelationExample queryCondition = getQueryCondition(qry);
        List<FileStoreRelation> qryResp = fileStoreRelationMapper.selectByExample(queryCondition);
        if (!qryResp.isEmpty()) {
            storageClient.destroy(qryResp.get(0).getStorageFileId());
            mongoFileStoreService.delFile(id);
        }
        return true;
    }
}
