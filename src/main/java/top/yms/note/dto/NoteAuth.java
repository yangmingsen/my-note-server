package top.yms.note.dto;

/**
 * 认证抽象实体接口
 */
public interface NoteAuth {
    String getUsername();
    String getPassword();
    default String getPin(){return null;}
}
