package top.yms.note.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.*;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.NoteLuceneIndex;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.utils.LocalThreadUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteDataService {

    private static final Logger log = LoggerFactory.getLogger(NoteDataService.class);

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Autowired
    private NoteDataVersionMapper noteDataVersionMapper;

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private FileStore fileStore;

//    @Autowired
//    @Qualifier(Constants.noteLuceneIndexMemoryQueue)
//    private NoteQueue noteQueue;

    @Autowired
    @Qualifier(Constants.noteLuceneSearch)
    private NoteDataIndexService noteDataIndexService;

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void addAndUpdate(NoteData noteData) {
        Long id = noteData.getId();
        NoteData dbNote = noteDataMapper.findById(id);
        if (checkContent(noteData.getContent())) {
            throw new BusinessException(NoteIndexErrorCode.E_203112);
        }
        Date opTime = new Date();
        if (dbNote == null) {
            noteData.setCreateTime(opTime);
            noteDataMapper.insert(noteData);
        } else {
            noteData.setUpdateTime(opTime);
            noteDataMapper.updateByPrimaryKeySelective(noteData);
        }

        //更新index信息
        NoteIndex noteIndex = new NoteIndex();
        noteIndex.setId(id);
        noteIndex.setUpdateTime(opTime);
        noteIndex.setSize((long)noteData.getContent().getBytes(StandardCharsets.UTF_8).length);
        noteIndexMapper.updateByPrimaryKeySelective(noteIndex);

        //通知更新lucene索引
        NoteIndex noteIndex1 = noteIndexMapper.selectByPrimaryKey(id);
        NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
        noteLuceneIndex.setId(id);
        noteLuceneIndex.setUserId(noteIndex1.getUserId());
        noteLuceneIndex.setParentId(noteIndex1.getParentId());
        noteLuceneIndex.setTitle(noteIndex1.getName());
        noteLuceneIndex.setContent(noteData.getContent());
        noteLuceneIndex.setIsFile(noteIndex1.getIsile());
        noteLuceneIndex.setType(noteIndex1.getType());
        noteLuceneIndex.setCreateDate(opTime);
        noteDataIndexService.update(noteLuceneIndex);

        //版本记录
        NoteDataVersion dataVersion = new NoteDataVersion();
        dataVersion.setNoteId(id);
        dataVersion.setContent(noteData.getContent());
        dataVersion.setUserId(noteData.getUserId());
        dataVersion.setCreateTime(opTime);
        noteDataVersionMapper.insertSelective(dataVersion);
    }

    private static final String [] ILLEGAL_LIST = {
            "<p><br></p>",
            "<p style=\"text-align: start;\"><br></p>"
    };
    private boolean checkContent(String content) {
        if (content == null || StringUtils.isBlank(content)) {
            return true;
        }
        for(String illegalStr : ILLEGAL_LIST) {
            if (content.equals(illegalStr)) {
                return true;
            }
        }

        return false;
    }

    private static final String [] SUPPORT_View_FILE = {
            "md","txt","java","xml","go","html","css","js","ts","vue","json","c","scala","yml",
            "cpp","py","bash","conf","ini"
    };

    private final ConcurrentHashMap<Long, Boolean> canPreviewCache = new ConcurrentHashMap<>();
    public boolean checkFileCanPreviewByCache(Long id) {
        Boolean canPreview = canPreviewCache.get(id);
        if ( canPreview != null) {
            return canPreview;
        }
        canPreview = checkFileCanPreview(id);
        canPreviewCache.put(id, canPreview);
        return canPreview;
    }

    /**
     * 检查当前文件是否可预览
     * @param id
     * @return
     */
    private boolean checkFileCanPreview(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        //1. 先通过noteIndex的f_type判断是否在 SUPPORT_View_FILE 列表中
        for (String st : SUPPORT_View_FILE) {
            if (st.equals(noteIndex.getType())) {
                return true;
            }
        }
        //2. 不在的话再去通过内容判断是否为文本。
        //todo 哎，这个判断算法还有问题，后续在看
        if (!Constants.MONGO.equals(noteIndex.getStoreSite())) {
            log.info("查询的文件id={}, 未存储在mongo上", id);
            //目前都是存储在mongo上的,
            return false;
        }

        AnyFile anyFile = fileStore.loadFile(noteIndex.getSiteId());
        if (anyFile.getLength() == 0L) {
            log.info("文件id={}, 为空文件", id);
            return false;
        }

        int bufferSize = 512;  // 读取前512字节来判断
        byte[] buffer = new byte[bufferSize];
        try(InputStream is = anyFile.getInputStream()) {
            int rLen = is.read(buffer);
            for (int i = 0; i < rLen; i++) {
                byte b = buffer[i];
                if (b < 0x09 || (b > 0x0A && b < 0x20) || b > 0x7E) {
                    return false;  // 如果发现不可打印字符，则不是文本文件
                }
            }
        } catch (Exception ee) {
            return false;
        }

        return true;
    }

    public NoteData findOne(Long id) {
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        NoteData noteData = new NoteData();
        if (Constants.MYSQL.equals(noteIndex.getStoreSite())) {
            noteData = noteDataMapper.selectByPrimaryKey(id);
        } else {
            //前提,当前文件要可预览, 目前使用markdown预览
            //因此 文本内容前后加了 " ```xxx  内容  ```` "
            if (!checkFileCanPreviewByCache(id)) {
                throw new BusinessException(NoteIndexErrorCode.E_203113);
            }
            AnyFile anyFile = fileStore.loadFile(noteIndex.getSiteId());

            StringBuilder contentStr = new StringBuilder("```");
            contentStr.append(noteIndex.getType()).append("\n");
            try(InputStreamReader isr = new InputStreamReader(anyFile.getInputStream(), StandardCharsets.UTF_8)) {
                int bufLen = 1024;
                char [] cBuf = new char[bufLen];
                int rLen = 0;
                while ((rLen = isr.read(cBuf)) > 0) {
                    contentStr.append(new String(cBuf, 0, rLen));
                }
            }catch (Exception e) {
                log.error("读取mongo文件内容出错", e);
            }

            contentStr.append("\n```");
            noteData.setId(id);
            noteData.setContent(contentStr.toString());
        }
        return noteData;
    }


    @Autowired
    private NoteFileMapper noteFileMapper;

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 20)
    public void syncDataSize() {
        Long uid = LocalThreadUtils.getUserId();
        noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(3).storeSite(Constants.MYSQL).get().example())
                .forEach(index -> {
                    Long id = index.getId();
                    NoteData noteData = noteDataMapper.selectByPrimaryKey(id);

                    if (noteData != null) {
                        NoteIndex upIndex = new NoteIndex();
                        upIndex.setId(id);
                        upIndex.setSize((long)noteData.getContent().getBytes(StandardCharsets.UTF_8).length);
                        noteIndexMapper.updateByPrimaryKeySelective(upIndex);
                    }

                });

        noteIndexMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(3).storeSite(Constants.MONGO).get().example())
                .forEach(index -> {
                    Long id = index.getId();
                    String fileId = index.getSiteId();
                    NoteFile noteFile = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(fileId).get().example()).get(0);

                    if (noteFile != null) {
                        NoteIndex upIndex = new NoteIndex();
                        upIndex.setId(id);
                        upIndex.setSize(noteFile.getSize());
                        noteIndexMapper.updateByPrimaryKeySelective(upIndex);
                    }

                });
    }

}
