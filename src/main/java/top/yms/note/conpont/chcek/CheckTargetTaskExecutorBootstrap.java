package top.yms.note.conpont.chcek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.config.SpringContext;

public class CheckTargetTaskExecutorBootstrap {

    private final static Logger log = LoggerFactory.getLogger(CheckTargetTaskExecutorBootstrap.class);

    public static void start() {
        CheckTargetTaskExecutor checkTargetTaskExecutor = SpringContext.getBean(CheckTargetTaskExecutor.class);
        Thread thread = new Thread(checkTargetTaskExecutor);
        thread.start();
        log.info("CheckTargetTaskExecutorBootstrap started......");
    }
}
