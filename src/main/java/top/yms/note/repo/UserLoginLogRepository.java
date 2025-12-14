package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.UserLoginLog;

public interface UserLoginLogRepository extends MongoRepository<UserLoginLog, String> {
}
