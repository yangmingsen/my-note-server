package top.yms.note.conpont;

public interface NoteEncryptService {
    /**
     * 笔记解密服务
     * @param id 笔记id
     */
    void decryptNote(Long id);

    /**
     * 笔记加密服务
     * @param id 笔记id
     */
    void encryptNote(Long id);
}
