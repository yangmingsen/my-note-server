package top.yms.note.conpont.note;

public interface NoteExport {
    /**
     * 检查支持该类型笔记导出
     * @param noteType note type
     * @param exportType 导出类型 pdf,docx,img...
     * @return
     */
    boolean supportExport(String noteType, String exportType);

    /**
     *  执行导出
     * @param noteId noteid
     * @param exportType 导出类型 pdf,docx,img...
     * @return
     */
    String export(Long noteId, String exportType) ;
}
