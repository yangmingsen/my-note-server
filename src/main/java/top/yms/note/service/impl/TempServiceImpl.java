package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.entity.AsyncFileSaveInfo;
import top.yms.note.entity.NetworkResourceInfo;
import top.yms.note.entity.RestOut;
import top.yms.note.mapper.NetworkResourceInfoMapper;
import top.yms.note.repo.AsyncFileSaveInfoRepository;
import top.yms.note.service.TempService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TempServiceImpl implements TempService {

    private final static Logger log = LoggerFactory.getLogger(TempServiceImpl.class);

    @Resource
    private NetworkResourceInfoMapper networkResourceInfoMapper;

    @Resource
    private AsyncFileSaveInfoRepository asyncFileSaveInfoRepository;

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
}
