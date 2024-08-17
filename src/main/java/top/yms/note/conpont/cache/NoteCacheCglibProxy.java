package top.yms.note.conpont.cache;

import top.yms.note.conpont.NoteCache;

public interface NoteCacheCglibProxy {
    void setTarget(Object target);
    void setCache(NoteCache noteCache);
    Object getTargetProxy();
}
