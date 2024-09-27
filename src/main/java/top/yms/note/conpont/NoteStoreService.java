package top.yms.note.conpont;


/**
 * note内容获取服务+更新
 *
 * 关系数据库未做事务处理，依赖调用层的@Transactional事务处理
 */
public interface NoteStoreService {

    /**
     * 查找某个
     * @param id
     * @return
     */
    Object findOne(Long id);

    /**
     * 新建+更新
     * @param note
     */
    void save(Object note);

    /**
     * 不适用
     * @param note
     */
    @Deprecated
    void update(Object note);
}
