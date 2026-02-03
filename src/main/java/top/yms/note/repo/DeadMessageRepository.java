package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.ChatNote;
import top.yms.note.entity.DeadMessage;

public interface DeadMessageRepository extends MongoRepository<DeadMessage, String> {

    ChatNote findByMsgId(String chatId);

}
