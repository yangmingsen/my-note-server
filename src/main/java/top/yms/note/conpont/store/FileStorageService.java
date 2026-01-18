package top.yms.note.conpont.store;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.entity.FileStoreRelationExample;
import top.yms.note.exception.CommonException;
import top.yms.note.mapper.FileStoreRelationMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.storage.client.StorageClient;
import top.yms.storage.entity.UploadResp;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Primary
@Component(NoteConstants.storageFileStoreService)
public class FileStorageService implements FileStoreService {

    private final static Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Qualifier(NoteConstants.mongoFileStoreService)
    @Resource
    private FileStoreService mongoFileStoreService;

    @Resource
    private StorageClient storageClient;

    @Resource
    private FileStoreRelationMapper fileStoreRelationMapper;

    @Resource
    private NoteRedisCacheService cacheService;

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
        String cKey = NoteCacheKey.NOTE_META_FILE_RELATION_KEY+qry.getCacheKey();
        Object cVal = cacheService.get(cKey);
        List<FileStoreRelation> qryResp;
        if (cVal != null) {
            qryResp = (List<FileStoreRelation>) cVal;
        } else {
            qryResp = fileStoreRelationMapper.selectByExample(queryCondition);
            cacheService.set(cKey, qry);
        }
        if (qryResp.isEmpty()) {
            AnyFile anyFile = mongoFileStoreService.loadFile(id);
            UploadResp uploadRsp = storageClient.upload(anyFile.getInputStream(), anyFile.getFilename());
            insertRelation(uploadRsp.getFileId(), id);
            return new StorageFile(uploadRsp.getFileId(), storageClient);
        }
        return new StorageFile(qryResp.get(0).getStorageFileId(), storageClient);
    }


    @Override
    public String saveFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFilename)) {
            int i = originalFilename.lastIndexOf("/");
            if (i > -1) {
                originalFilename = originalFilename.substring(i+1);
            }
        }
        //bug2020807 若是目录上传，比如 xx/aa.xml 会导致file-storage服务存储时还建立一个xx目录再去存aa.xml
//        UploadResp uploadResp = storageClient.upload(file.getInputStream(), file.getOriginalFilename());
        UploadResp uploadResp = storageClient.upload(file.getInputStream(), originalFilename);
        String id = mongoFileStoreService.saveFile(file);
        insertRelation(uploadResp.getFileId(), id);
        return id;
    }

    @Override
    public String saveFile(File file) throws Exception {
        UploadResp uploadResp = storageClient.upload(file);
        String id = mongoFileStoreService.saveFile(file);
        insertRelation(uploadResp.getFileId(), id);
        return id;
    }

    private void insertRelation(String storageFileId, String mongoFileId) {
        FileStoreRelation fsr = new FileStoreRelation();
        fsr.setMongoFileId(mongoFileId);
        fsr.setStorageFileId(storageFileId);
        fsr.setCreateTime(new Date());
        fileStoreRelationMapper.insertSelective(fsr);
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
        String cKey = NoteCacheKey.NOTE_META_FILE_RELATION_KEY+qry.getCacheKey();
        Object cVal = cacheService.get(cKey);
        List<FileStoreRelation> qryResp;
        if (cVal != null) {
            qryResp = (List<FileStoreRelation>) cVal;
            cacheService.del(cKey);
        } else {
            qryResp = fileStoreRelationMapper.selectByExample(queryCondition);
        }
        if (!qryResp.isEmpty()) {
            storageClient.destroy(qryResp.get(0).getStorageFileId());
            mongoFileStoreService.delFile(id);
        }
        return true;
    }

    public String saveFile(InputStream inputStream, Map<String, Object> option) {
        String fileName = (String)option.get(NoteConstants.OPTION_FILE_NAME);
        String fileType = (String)option.get(NoteConstants.OPTION_FILE_TYPE);
        if (StringUtils.isBlank(fileName)) {
            log.error("Empty Error: fileName={}, fileType={}", fileName, fileType);
            throw new CommonException(CommonErrorCode.E_200202);
        }
        UploadResp uploadResp = storageClient.upload(inputStream, fileName +"."+ fileType);
        String storageFileId = uploadResp.getFileId();
        //为什么这么做？ 重新获取文件流再给mongo存储服务，因为inputStream在storageClient.upload后就被close了，所以只能再从storage中获取
        InputStream fileStream = storageClient.getFileStream(storageFileId);
        String mongoFileId = mongoFileStoreService.saveFile(fileStream, option);
        insertRelation(storageFileId, mongoFileId);
        return mongoFileId;
    }

    public String getStringContent(String id) {
        AnyFile anyFile = loadFile(id);
        StringBuilder contentStr = new StringBuilder();
        try(InputStream is = anyFile.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            int bufLen = 1024;
            char [] cBuf = new char[bufLen];
            int rLen;
            while ((rLen = isr.read(cBuf)) > 0) {
                contentStr.append(new String(cBuf, 0, rLen));
            }
        }catch (Exception e) {
            log.error("读取mongo文件内容出错", e);
            throw new RuntimeException(e);
        }
        if (contentStr.length() == 0) {
            return null;
        }
        return contentStr.toString();
    }
}
