package top.yms.note.conpont;

public interface NoteExportService {
    /**
     * 笔记导出
     * @param noteId note id
     * @param exportType 导出类型
     * @return
     */
    String export(Long noteId, String exportType);
}
