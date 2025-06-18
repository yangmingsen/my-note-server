package top.yms.note.service.impl;

import org.springframework.stereotype.Component;
import top.yms.note.other.ChatSync;
import top.yms.note.service.NoteSyncService;

import javax.annotation.Resource;

@Component
public class NoteSyncServiceImpl implements NoteSyncService {

    @Resource
    private ChatSync chatSync;

    @Override
    public void syncChatNote() {
        chatSync.getChatNote();
    }
}
