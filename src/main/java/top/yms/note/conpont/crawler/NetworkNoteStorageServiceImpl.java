package top.yms.note.conpont.crawler;

import org.springframework.stereotype.Component;
import top.yms.note.entity.NetworkNote;
import top.yms.note.repo.NetworkNoteRepository;

import javax.annotation.Resource;

@Component
public class NetworkNoteStorageServiceImpl implements NetworkNoteStorageService {

    @Resource
    private NetworkNoteRepository networkNoteRepository;

    @Override
    public boolean exists(String md5Id) {
        NetworkNote networkNote = networkNoteRepository.findByMd5Id(md5Id);
        return networkNote != null;
    }

}
