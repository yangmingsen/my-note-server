package top.yms.note.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStore;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteIndexMapper;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteDataService {

    private static Logger log = LoggerFactory.getLogger(NoteDataService.class);

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private FileStore fileStore;

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Exception.class, timeout = 10)
    public void addAndUpdate(NoteData noteData) {
        Long id = noteData.getId();
        NoteData dbNote = noteDataMapper.findById(id);
        if (checkContent(noteData.getContent())) {
            throw new BusinessException(NoteIndexErrorCode.E_203112);
        }
        if (dbNote == null) {
            noteData.setCreateTime(new Date());
            noteDataMapper.insert(noteData);
        } else {
            Date upTime = new Date();
            noteData.setUpdateTime(upTime);
            noteDataMapper.updateByPrimaryKeySelective(noteData);

            NoteIndex noteIndex = new NoteIndex();
            noteIndex.setId(id);
            noteIndex.setUpdateTime(upTime);
            noteIndexMapper.updateByPrimaryKeySelective(noteIndex);
        }

    }

    private static final String [] ILLEGAL_LIST = {
            "<p><br></p>"
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
            "cpp","py","bash",
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

    public NoteData get(Long id) {
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
            int bufLen = 1024;
            byte [] buf = new byte[bufLen];
            try (InputStream is = anyFile.getInputStream()) {
                int rLen;
                while ((rLen = is.read(buf)) > 0) {
                    contentStr.append(new String(buf, 0, rLen));
                }
            }catch (Exception e) {
                log.error("读取mongo文件内容出错", e);
            }
            contentStr.append("\n```");
            noteData.setId(id);
            noteData.setContent(contentStr.toString());
        }

        log.info("get id={}, dataSize={}", id, RamUsageEstimator.humanSizeOf(noteData));
        return noteData;
    }
}
