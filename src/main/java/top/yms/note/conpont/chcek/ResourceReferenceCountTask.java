package top.yms.note.conpont.chcek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.entity.CheckTarget;
import top.yms.note.repo.ResourceReferenceCountRepository;

import javax.annotation.Resource;

/**
 * <h2>资源引用检查</h2>
 * <p>检查t_note_file记录引用情况</p>
 */
@Component
public class ResourceReferenceCountTask extends AbstractCheckTargetTask{

    private static final Logger log = LoggerFactory.getLogger(ResourceReferenceCountTask.class);

    @Resource
    private ResourceReferenceCountRepository resourceReferenceCountRepository;

    public boolean support(String name) {
        return "resource-ref-check".equals(name);
    }

    @Override
    void doCheckTask(CheckTarget checkTarget) {

    }
}
