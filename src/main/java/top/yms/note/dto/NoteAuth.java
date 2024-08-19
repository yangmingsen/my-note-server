package top.yms.note.dto;

public interface NoteAuth {
    String getUsername();
    String getPassword();
    default String getPin(){return null;}
}
