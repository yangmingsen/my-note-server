package top.yms.note.conpont.export;

public interface NoteFileExport {
    /**
     * 笔记转换
     * @param id noteId
     * @param fromType 从
     * @param toType 到
     * @return
     */
    String noteExport(Long id, String fromType, String toType);
}
