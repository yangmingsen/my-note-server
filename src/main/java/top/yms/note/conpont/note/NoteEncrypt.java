package top.yms.note.conpont.note;

public interface NoteEncrypt {
    boolean supportEncrypt();

    /**
     * 笔记解密
     * @param id
     * @return
     */
    boolean noteDecrypt(Long id);

    /**
     * 笔记加密
     * @param id
     * @return
     */
    boolean noteEncrypt(Long id);
}
