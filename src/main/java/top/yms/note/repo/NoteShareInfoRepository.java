package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.NoteShareInfo;

public interface NoteShareInfoRepository extends MongoRepository<NoteShareInfo, String> {
    /**
     * 查找分析信息通过noteId
     * @param noteId noteId
     * @return
     */
    NoteShareInfo findByNoteId(Long noteId);

}
