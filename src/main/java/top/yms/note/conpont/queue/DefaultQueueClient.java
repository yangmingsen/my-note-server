package top.yms.note.conpont.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.queue.channel.MemoryChannel;
import top.yms.note.conpont.queue.channel.QueueChannel;
import top.yms.note.conpont.task.NoteTask;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class DefaultQueueClient implements InitializingBean, QueueClient{

    private final static Logger log = LoggerFactory.getLogger(DefaultQueueClient.class);

    @Resource
    private QueueChannel queueChannel;

    private Thread highLoopThread = null ;
    private Thread otherLoopThread = null ;

    private BlockingQueue<Runnable> taskQueue = null;

    private ThreadPoolExecutor executor = null;

    @Resource
    private ConsumerService consumerService;

    public void afterPropertiesSet() throws Exception {
        log.info("=======start AutoConsumerService==========");
        taskQueue = new ArrayBlockingQueue<>(500);
        executor = new ThreadPoolExecutor(2, 10, 60, TimeUnit.SECONDS, taskQueue, new ThreadPoolExecutor.CallerRunsPolicy());
        highLoopThread = new Thread(() -> {
            while (true) {
                try {
                    final IMessage iMessage = queueChannel.takeFromHigh();
                    executor.execute(() -> consumerService.consumer(iMessage));
                } catch (Throwable th) {
                    DefaultQueueClient.log.error("highLoopThread error", th);
                }
            }
        });
        highLoopThread.start();
        otherLoopThread = new Thread(() -> {
            long cnt = 0;
            while (true) {
                try {
                    final IMessage iMessage = queueChannel.pollFromMedium(1, TimeUnit.SECONDS);
                    if (iMessage != null) {
                        executor.execute(() -> consumerService.consumer(iMessage));
                    }
                    if (cnt % 3 == 0) {
                        final IMessage iMessage2 = queueChannel.pollFromLow();
                        if (iMessage2 != null) {
                            executor.execute(() -> consumerService.consumer(iMessage2));
                        }
                    }
                    cnt++;
                } catch (Throwable th) {
                    DefaultQueueClient.log.error("highLoopThread error", th);
                }
            }
        });
        otherLoopThread.start();
        log.info("=======start AutoConsumerService ok==========");
    }

    @Override
    public boolean send(IMessage message) {
        return queueChannel.offer(message);
    }

    @Override
    public IMessage receive() {
        return null;
    }
}
