package top.yms.note.conpont.chcek;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteService;
import top.yms.note.conpont.task.NoteExecuteService;
import top.yms.note.conpont.task.NoteTask;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.*;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.repo.ResourceReferenceCountRepository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <h2>资源引用检查</h2>
 * <p>检查t_note_file记录引用情况</p>
 */
@Component
public class ResourceReferenceCountTask extends AbstractCheckTargetTask implements NoteTask {

    private static final Logger log = LoggerFactory.getLogger(ResourceReferenceCountTask.class);

    @Resource
    private ResourceReferenceCountRepository resourceReferenceCountRepository;

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private NoteDataVersionMapper noteDataVersionMapper;

    //存储noteId <=> List<NoteFile>
    private   Map<Long, List<NoteFile>> mapList ;

    //存储fileIds
    private  final Set<NoteFile> unRefFileIdSet = new HashSet<>();

    @Qualifier(NoteConstants.noteThreadPoolExecutor)
    @Resource
    private NoteExecuteService noteExecuteService;

    private AtomicInteger curIdx;

    private int totalSize;

    public int getSortValue() {
        return 1;
    }

    public boolean support(String name) {
        return "resource-ref-check".equals(name);
    }

    private void init() {
        unRefFileIdSet.clear();
        curIdx = new AtomicInteger(0);
    }

    private void updateProgress(int num) {
        int cnt = curIdx.get();
        cnt += num;
        curIdx.set(cnt);
    }

    @Override
    void doCheckTask(CheckTarget checkTarget) throws Exception {
        //初始化
        init();
        //获取数据
        List<NoteFile> noteFileList = noteFileMapper.findAll();
        totalSize = noteFileList.size();
        //进度条开启
        noteExecuteService.execute(this);
        //parse data
        mapList = noteFileList.stream().collect(Collectors.groupingBy(NoteFile::getNoteRef));
        for (Map.Entry<Long, List<NoteFile>> entry : mapList.entrySet()) {
            Long noteId = entry.getKey();
            List<NoteFile> value = entry.getValue();
            NoteMeta noteMeta = noteMetaMapper.selectByPrimaryKey(noteId);
            if (noteMeta == null) {//未找到情况
                for (NoteFile noteFile : value) {
                    unRefFileIdSet.add(noteFile);
                }
                updateProgress(value.size());
                continue;
            }
            //找到情况
            String type = noteMeta.getType();
            if (StringUtils.equalsAny(type, NoteConstants.markdownSuffix, NoteConstants.defaultSuffix)) {
                //内置笔记类型处理
                NoteData noteData = noteDataMapper.selectByPrimaryKey(noteId);
                if (noteData == null) {
                    log.error("未找到noteData, noteId={}", noteId);
                    updateProgress(value.size());
                    continue;
                }
                String content = noteData.getContent();
                if (StringUtils.isBlank(content)) {
                    log.error("发现空内容，noteId={}", noteId);
                    updateProgress(value.size());
                    continue;
                }
                for (NoteFile noteFile : value) {
                    String fileId = noteFile.getFileId();
                    boolean unRefFlag = false;
                    if (StringUtils.contains(content, fileId))  {
                        continue;
                    }
                    List<NoteDataVersion> noteDataVersionList = noteDataVersionMapper.selectByNoteId(noteId);
                    for (NoteDataVersion dataVersion : noteDataVersionList) {
                        String content1 = dataVersion.getContent();
                        if (StringUtils.contains(content1, fileId))  {
                            //包含
                            unRefFlag = true;
                            break;
                        }
                    }
                    if (!unRefFlag) { //都不包含
                        unRefFileIdSet.add(noteFile);
                    }
                }
            } else {
                //应该是1对1的场景
                if (value.size() > 1) {
                    log.error("异常数据noteId={}, fileId={}", noteId, value.stream().map(NoteFile::getFileId).collect(Collectors.toList()));
                }
            }
            updateProgress(value.size());
        }
        log.info("扫描完成发现未引用数据量：{}", unRefFileIdSet.size());
        for (NoteFile noteFile : unRefFileIdSet) {
            String fileId = noteFile.getFileId();
            ResourceReferenceCount rrc = new ResourceReferenceCount();
            rrc.setNoteId(noteFile.getNoteRef());
            rrc.setResourceId(fileId);
            rrc.setCheckDate(new Date());
            ResourceReferenceCount resource = resourceReferenceCountRepository.findByResourceId(fileId);
            if (resource == null) {
                resourceReferenceCountRepository.save(rrc);
            }
        }
    }

    @Override
    public void run() {
        log.info("=============开始资源Reference检查=================");
        while (true) {
            int idx = curIdx.get();
            double progress = ((idx*1.0) / (totalSize*1.0))*100.0d;
            String  progressStr = String.format("%.2f",progress);
            log.info("progress = {}%", progressStr);
            if (idx >= totalSize) {
                break;
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
            }
        }
        log.info("=============结束资源Reference检查=================");
    }
}
