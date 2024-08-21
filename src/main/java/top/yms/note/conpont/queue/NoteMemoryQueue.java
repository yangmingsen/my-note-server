package top.yms.note.conpont.queue;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.NoteQueue;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by yangmingsen on 2024/8/21.
 */

public class NoteMemoryQueue implements NoteQueue {

    private final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(100);

    @Override
    public boolean offer(Object o) {
        return queue.offer(o);
    }

    @Override
    public Object take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
