package top.yms.note.conpont.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.Constants;
import top.yms.note.comm.NoteIndexErrorCode;
import top.yms.note.conpont.AnyFile;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/9/27.
 */
@Component
public class PreviewNoteType extends AbstractNoteType implements NotePreview{

    private final static Logger log = LoggerFactory.getLogger(PreviewNoteType.class);

    private static final String [] SUPPORT_View_FILE = {
            "txt","java","xml","go","html","css","js","ts","vue","json","c","scala","yml",
            "cpp","py","bash","conf","ini","sql","cnf"
    };

    private static final Map<String,Boolean> supportMap = new HashMap<>();

    static {
        for(String type : SUPPORT_View_FILE) {
            supportMap.put(type, Boolean.TRUE);
        }
    }

    @Override
    public boolean support(String type) {
        return supportMap.get(type) == Boolean.TRUE;
    }

    @Override
    public Object doGetContent(Long id) {
        //前提,当前文件要可预览, 目前使用markdown预览
        //因此 文本内容前后加了 " ```xxx  内容  ```` "
        if (!checkFileCanPreviewByCache(id)) {
            throw new BusinessException(NoteIndexErrorCode.E_203113);
        }
        NoteData noteData = new NoteData();
        NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);

        //目前访问必须在mongo上
        if (!Constants.MONGO.equals(noteIndex.getStoreSite())) {
            throw new BusinessException(NoteIndexErrorCode.E_203119);
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

        return noteData;
    }

    @Override
    public void save(Object data) throws BusinessException {
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
}
