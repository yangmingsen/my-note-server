package top.yms.note.conpont.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.conpont.AnyFile;
import top.yms.note.entity.NoteFile;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/10/5.
 */

public class CsvPreviewNoteType extends AbstractNoteType{

    private final static Logger log = LoggerFactory.getLogger(CsvPreviewNoteType.class);

    private final static String supportType = "csv";

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }

    @Override
    public void save(Object data) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    /**
     *
     * @param id noteId
     * @return
     */
    public Object doGetContent(Long id) {
        NoteFile noteFile = findNoteFile(id);
        AnyFile anyFile = fileStore.loadFile(noteFile.getFileId());

        try {
            
        } catch (Exception e) {
            log.error("doGetContent异常：", e);
        }

        return null;

    }
}
