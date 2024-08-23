package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class CommonNoteType extends AbstractNoteType {
    @Override
    public boolean support(String type) {
        return false;
    }
}
