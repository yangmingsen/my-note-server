package top.yms.note.conpont;

import java.util.concurrent.TimeUnit;

/**
 * 队列服务
 */
public interface NoteQueue {

    //入队api
    boolean offer(Object t);

    default boolean offer(Object e, long timeout, TimeUnit unit) {
        return false;
    }

    //出队api
    Object take();

}
