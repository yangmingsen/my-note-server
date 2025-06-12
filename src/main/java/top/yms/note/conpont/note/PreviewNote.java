package top.yms.note.conpont.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.req.NoteShareReqDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.NoteShareInfo;
import top.yms.note.exception.BusinessException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.ComponentErrorCode;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.vo.NoteShareVo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/9/27.
 */
@Component
public class PreviewNote extends AbstractNote implements NotePreview, InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(PreviewNote.class);

    @Value("${note.preview.support}")
    private String supportViewNoteList;

    public int getSortValue() {
        return 5;
    }

    private final Set<String> typeSet = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("preview support type: {}", supportViewNoteList);
        String[] noteType = supportViewNoteList.split(",");
        typeSet.addAll(Arrays.asList(noteType));
    }

    @Override
    public boolean support(String type) {
        return typeSet.contains(type);
    }

    @Override
    public INoteData doGetContent(Long id) {
        //前提,当前文件要可预览, 目前使用markdown预览
        //因此 文本内容前后加了 " ```xxx  内容  ```` "
        if (!checkFileCanPreviewByCache(id)) {
            throw new BusinessException(NoteIndexErrorCode.E_203113);
        }
        NoteData noteData = new NoteData();
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        //目前访问必须在mongo上
        if (!NoteConstants.MONGO.equals(noteIndex.getStoreSite())) {
            throw new BusinessException(NoteIndexErrorCode.E_203119);
        }
        AnyFile anyFile = fileStoreService.loadFile(noteIndex.getSiteId());
        StringBuilder contentStr = new StringBuilder("```");
        contentStr.append(noteIndex.getType()).append("\n");
        try( InputStream is = anyFile.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
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
        return noteData;
    }

    @Override
    public void doSave(INoteData iNoteData) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public boolean canPreview(Long id) {
        return checkFileCanPreviewByCache(id);
    }

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
        if (typeSet.contains(noteIndex.getType())) {
            return true;
        }
        //2. 不在的话再去通过内容判断是否为文本。
        //todo 哎，这个判断算法还有问题，后续在看
        if (!NoteConstants.MONGO.equals(noteIndex.getStoreSite())) {
            log.debug("查询的文件id={}, 未存储在mongo上", id);
            //目前都是存储在mongo上的,
            return false;
        }

        AnyFile anyFile = fileStoreService.loadFile(noteIndex.getSiteId());
        if (anyFile.getLength() == 0L) {
            log.debug("文件id={}, 为空文件", id);
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


    public NoteLuceneIndex findNoteLuceneDataOne(Long id) {
        NoteLuceneIndex noteLuceneIndex = packNoteIndexForNoteLuceneIndex(id);
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
        if (StringUtils.isEmpty(noteIndex.getSiteId())) {
            throw new BusinessException(ComponentErrorCode.E_204000);
        }
        String stringContent = fileStoreService.getStringContent(noteIndex.getSiteId());
        noteLuceneIndex.setContent(stringContent);

        return noteLuceneIndex;
    }

    public boolean supportShare(String noteType) {
        return support(noteType);
    }

    public NoteShareVo doShareNoteGet(NoteShareReqDto noteShareReqDto) {
        Long noteId = noteShareReqDto.getNoteIndex().getId();
        INoteData iNoteData = doGetContent(noteId);
        NoteShareInfo noteShareInfo = noteShareInfoRepository.findByNoteId(noteId);
        //ret
        NoteShareVo resp = new NoteShareVo();
        resp.setNoteIndex(noteShareReqDto.getNoteIndex());
        resp.setNoteData((NoteData) iNoteData);
        resp.setNoteShareInfo(noteShareInfo);
        return resp;
    }

    public void afterShareNoteGet(NoteShareVo noteShareVo) {}
}
