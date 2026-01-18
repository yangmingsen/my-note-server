package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.NetworkNote;

public interface NetworkNoteRepository  extends MongoRepository<NetworkNote, String> {

    NetworkNote findByMd5Id(String md5Id);

    NetworkNote findByNoteId(Long noteId);

}
