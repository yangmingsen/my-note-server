package top.yms.note.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.entity.AsyncFileSaveInfo;
import top.yms.note.entity.FileStoreRelation;
import top.yms.note.entity.NetworkResourceInfo;
import top.yms.note.entity.RestOut;
import top.yms.note.mapper.NetworkResourceInfoMapper;
import top.yms.note.repo.AsyncFileSaveInfoRepository;
import top.yms.note.service.FileStoreRelationService;
import top.yms.note.service.TempService;
import top.yms.storage.client.StorageClient;
import top.yms.storage.entity.FileMetaVo;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TempServiceImpl implements TempService {

    private final static Logger log = LoggerFactory.getLogger(TempServiceImpl.class);

    @Resource
    private NetworkResourceInfoMapper networkResourceInfoMapper;

    @Resource
    private AsyncFileSaveInfoRepository asyncFileSaveInfoRepository;

    @Resource(name = NoteConstants.noteRedisCacheServiceImpl)
    private NoteRedisCacheService noteRedisCacheService;

    @Resource
    private StorageClient storageClient;

    @Resource
    private FileStoreRelationService fileStoreRelationService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 240)
    public RestOut<String> networkResourceInfoFromMongoToMysql() {
        List<AsyncFileSaveInfo> list = asyncFileSaveInfoRepository.findAll();
        int batchSize = 1000;
        int totalSize = list.size();
        for (int i = 0; i < totalSize; i += batchSize) {
            int end = Math.min(i + batchSize, totalSize);
            List<NetworkResourceInfo> batchList = list.subList(i, end).stream().filter(fsd -> fsd.getFetchUrl().length() < 500 && fsd.getSuffix().length() < 10).map(fileSaveDto -> {
                NetworkResourceInfo networkResourceInfo = new NetworkResourceInfo();
                String noteFileId = fileSaveDto.getNoteFileId();
                networkResourceInfo.setNoteFileId(noteFileId);
                networkResourceInfo.setUrl(fileSaveDto.getFetchUrl());
                networkResourceInfo.setSuffix(fileSaveDto.getSuffix());
                networkResourceInfo.setName(fileSaveDto.getTmpFileName());
                networkResourceInfo.setCreateTime(new Date());
                return networkResourceInfo;
            }).collect(Collectors.toList());
            int insertedCount = networkResourceInfoMapper.insertBatch(batchList);
            // 可选：验证插入数量
            if (insertedCount != batchList.size()) {
                throw new RuntimeException("批量插入数量不匹配");
            }
            // 可选：每批插入后清理缓存或记录日志
            if ((i / batchSize + 1) % 10 == 0) {
                log.info("已处理 {} / {} 条记录", end, totalSize);
            }
        }
        log.info("批量插入完成，共插入 {} 条记录", totalSize);
        return RestOut.succeed("ok");
    }

    @Override
    public RestOut<String> reduceFileStorageCap() {
        Map<Object, Object> imageMap = noteRedisCacheService.hGetAll(NoteCacheKey.ASYNC_UPLOAD_FILE_DUP_CHECK_HASH);
        for (Map.Entry<Object, Object> kv : imageMap.entrySet()) {
            try {
                String url = (String)kv.getKey();
                String noteFileId = (String)kv.getValue();
                noteFileId = noteFileId.split("id=")[1];
                FileStoreRelation fileStoreRelation = fileStoreRelationService.findOneByNoteFileId(noteFileId);
                if (fileStoreRelation == null) {
                    continue;
                }
                String storageFileId = fileStoreRelation.getStorageFileId();
                FileMetaVo fileMetaInfo = storageClient.getFileMetaInfo(storageFileId);
                if (fileMetaInfo == null) {
                    continue;
                }
                NetworkResourceInfo oldV = networkResourceInfoMapper.selectByPrimaryKey(noteFileId);
                if (oldV != null) {
                    continue;
                }
                NetworkResourceInfo networkResourceInfo = new NetworkResourceInfo();
                networkResourceInfo.setNoteFileId(noteFileId);
                networkResourceInfo.setName(fileMetaInfo.getName());
                networkResourceInfo.setSuffix(fileMetaInfo.getName().split("/")[1]);
                networkResourceInfo.setUrl(url);
                networkResourceInfo.setCreateTime(new Date());
                networkResourceInfoMapper.insertSelective(networkResourceInfo);
                //del fileStorage
                storageClient.destroy(storageFileId);
            } catch (Exception e) {
                log.error("imageMap foreach error", e);
            }
        }
        return RestOut.succeed("ok");
    }
}
