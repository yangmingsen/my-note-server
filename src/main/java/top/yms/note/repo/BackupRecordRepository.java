package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.BackupRecord;

public interface BackupRecordRepository extends MongoRepository<BackupRecord, String> {

}
