package top.yms.note.conpont.export;

import top.yms.note.conpont.ComponentSort;

public interface NoteConvert extends ComponentSort, Comparable<ComponentSort> {
    boolean support(String fromType, String toType);
    String convert(Long id);
}
