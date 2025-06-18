package top.yms.note.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import top.yms.note.entity.ChatNote;

public interface ChatNoteRepository extends MongoRepository<ChatNote, String> {

    ChatNote findByChatId(String chatId);

    ChatNote findByNoteId(Long noteId);

}
