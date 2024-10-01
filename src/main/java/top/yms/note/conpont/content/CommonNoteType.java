package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.exception.BusinessException;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class CommonNoteType extends AbstractNoteType {
    @Override
    public boolean support(String type) {
        return false;
    }

    @Override
    public Object doGetContent(Long id) {
        throw new BusinessException(CommonErrorCode.E_200211);
    }

    @Override
    public void save(Object data) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200211);
    }
}
