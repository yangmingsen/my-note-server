package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.AsyncFileSaveInfo;

public interface AsyncFileSaveInfoRepository extends MongoRepository<AsyncFileSaveInfo, String> {

    AsyncFileSaveInfo findByNoteFileId(String noteFileId);

}
