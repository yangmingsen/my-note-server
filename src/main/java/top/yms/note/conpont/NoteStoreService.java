package top.yms.note.conpont;


import top.yms.note.dto.INoteData;

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
    INoteData findOne(Long id);

    /**
     * 新建+更新
     * <p>注意：在你需要事务控制时，请不要使用此方法，因为该方法无事务，
     *      请使用top.yms.note.service.NoteDataService#save(top.yms.note.dto.INoteData)</p>
     * @param
     */
    void save(INoteData iNoteData);

    /**
     * 不适用
     * @param
     */
    @Deprecated
    void update(INoteData iNoteData);

    /**
     * 删除笔记
     * @param id
     */
    void destroy(Long id);
}
