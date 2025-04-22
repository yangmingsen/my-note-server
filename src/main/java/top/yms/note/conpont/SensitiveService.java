package top.yms.note.conpont;

public interface SensitiveService {
    /**
     * 是否存在敏感内容
     * @param id noteid
     * @return true 存在
     */
    boolean isSensitive(Long id);
}
