package top.yms.note.conpont.note;

public interface NoteDestroy {

    boolean supportDestroy(String noteType);

    void noteDestroy(Long id);
}
