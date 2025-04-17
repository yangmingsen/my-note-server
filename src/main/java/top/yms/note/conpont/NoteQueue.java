package top.yms.note.conpont;

import java.util.concurrent.TimeUnit;

/**
 * 队列服务
 * <p>目前未使用</p>
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
