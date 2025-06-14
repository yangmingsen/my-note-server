package top.yms.note.conpont.export;

import top.yms.note.conpont.ComponentComparable;


public interface NoteConvert extends ComponentComparable {

    boolean support(String fromType, String toType);

    String convert(Long id);
}
