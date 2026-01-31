package top.yms.note.conpont.crawler;

import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.store.AsyncFileSaveDto;
import top.yms.note.entity.NoteFile;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CrawlerImageUploader implements ImageUploader {

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private IdWorker  idWorker;

    @Resource
    private NoteRedisCacheService cacheService;

    @Override
    public String upload(InputStream inputStream, String suffix, Consumer<NoteFile> consumer) {
        String tmpFileName = "临时图片"+idWorker.nextId();
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put(NoteConstants.OPTION_FILE_NAME, tmpFileName);
        optionMap.put(NoteConstants.OPTION_FILE_TYPE, suffix);
        String fileId = fileStoreService.saveFile(inputStream, optionMap);
        //do file info add
        final NoteFile noteFile = new NoteFile();
        noteFile.setFileId(fileId);
        noteFile.setName(tmpFileName+"."+suffix);
        noteFile.setType(suffix);
        noteFile.setUrl(NoteConstants.getFileViewUrlSuffix(fileId));
        noteFile.setViewCount(0L);
        noteFile.setCreateTime(new Date());
        consumer.accept(noteFile);
        //get url
        String url = NoteConstants.getBaseUrl()+NoteConstants.getFileViewUrlSuffix(fileId);
        //ret access url
        return url;
    }


    @Override
    public String asyncUpload(String imgUrl, String suffix, Consumer<NoteFile> consumer) {
        String tmpFileName = "临时图片"+idWorker.nextId();
        String fileId = idWorker.nextId()+"";
        //进入队列
        AsyncFileSaveDto asyncFileSaveDto = new AsyncFileSaveDto();
        asyncFileSaveDto.setFetchUrl(imgUrl);
        asyncFileSaveDto.setNoteFileId(fileId);
        asyncFileSaveDto.setSuffix(suffix);
        asyncFileSaveDto.setTmpFileName(tmpFileName);
        cacheService.rPush(NoteCacheKey.ASYNC_UPLOAD_FILE_LIST, asyncFileSaveDto);
        //do file info add
        final NoteFile noteFile = new NoteFile();
        noteFile.setFileId(fileId);
        noteFile.setName(tmpFileName+"."+suffix);
        noteFile.setType(suffix);
        noteFile.setUrl(NoteConstants.getFileViewUrlSuffix(fileId));
        noteFile.setViewCount(0L);
        noteFile.setCreateTime(new Date());
        consumer.accept(noteFile);
        //get url
        String url = NoteConstants.getBaseUrl()+NoteConstants.getFileViewUrlSuffix(fileId);
        //ret access url
        return url;
    }
}

