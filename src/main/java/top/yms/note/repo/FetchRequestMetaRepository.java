package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.FetchRequestMeta;

public interface FetchRequestMetaRepository extends MongoRepository<FetchRequestMeta, String> {

    FetchRequestMeta findByNoteId(Long noteId);

}
