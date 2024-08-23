package top.yms.note.conpont;


/**
 * note内容获取服务
 */
public interface NoteStoreService {

    Object findOne(Long id);

    void save(Object note);

    void update(Object note);
}
