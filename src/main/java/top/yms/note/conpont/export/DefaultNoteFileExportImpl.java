package top.yms.note.conpont.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.exception.BusinessException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class DefaultNoteFileExportImpl implements NoteFileExport, ApplicationListener<ApplicationReadyEvent> {

    private static  final Logger log = LoggerFactory.getLogger(DefaultNoteFileExportImpl.class);

    private final List<NoteConvert> convertList = new LinkedList<>();

    @Override
    public String noteExport(Long id, String fromType, String toType) {
        for(NoteConvert noteConvert : convertList) {
            if (noteConvert.support(fromType, toType)) {
                return noteConvert.convert(id);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200222);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        convertList.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        context, NoteConvert.class, true, false).values());
        Collections.sort(convertList);
        log.info("获取到convertList: {}", convertList);
    }
}
