package top.yms.note.conpont;

/**
 * note destroy service
 * <p>destroy note should call this...</p>
 */
public interface NoteDestroyService {

    boolean supportDestroy(String noteType);

    void noteDestroy(Long id);
}
