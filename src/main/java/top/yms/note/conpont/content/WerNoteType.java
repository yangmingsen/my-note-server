package top.yms.note.conpont.content;

import org.springframework.stereotype.Component;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component
public class WerNoteType extends AbstractNoteType {

    private final static String supportType = "wer";

    @Override
    public boolean support(String type) {
        return supportType.equals(type);
    }
}
