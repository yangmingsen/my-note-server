package top.yms.note.conpont;

public interface NoteCache {

    Object find(String id);

    Object find();

    Object add(String id, Object data);

    Object delete(String id);

    Object update(String id, Object data);

    default void clear() {}
}
