package top.yms.note.conpont.task;

import com.alibaba.fastjson2.JSONObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.ComponentSort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangmingsen on 2024/10/2.
 */

public abstract class AbstractAsyncExecuteTask implements AsyncExecuteTask{

    private final static Logger log = LoggerFactory.getLogger(AbstractAsyncExecuteTask.class);

    /**
     * 当前状态：0, 未在允许; 1,运行中
     */
    private volatile int status;

    private enum StatusEnum {
        UnRunning(0, "未运行中"),
        Running(1, "运行中"),
        ;
        private final int value;
        private final String name;

        StatusEnum(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static StatusEnum apply(int v) {
            for (StatusEnum nt : StatusEnum.values()) {
                if (nt.value == v) {
                    return nt;
                }
            }
            return null;
        }

    }

    private final AtomicInteger workerCount = new AtomicInteger(0);

    protected List<AsyncTask> dataList = new LinkedList<>();

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    protected MongoTemplate mongoTemplate;


    @Override
    public int compareTo(ComponentSort other) {
        return this.getSortValue() - other.getSortValue();
    }

    @Override
    public int getSortValue() {
        return 9999;
    }

    @Override
    public synchronized void addTask(AsyncTask task) {
        dataList.add(task);

        JSONObject taskInfo = JSONObject.from(task);
        Document newDoc = Document.parse(taskInfo.toString());
        mongoTemplate.save(newDoc, NoteConstants.taskInfoMessage);
    }


    /**
     * 是否需要事务. true 需要
     * @return
     */
    abstract boolean needTx() ;

    @Override
    public void run() {
        if (!beforeRun()) {
            log.warn("beforeRun 异常....");
        }

        if (hasData()) {
            if (needTx()) {
                TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
                try {
                    doRun();
                    transactionManager.commit(status);
                } catch (Exception e) {
                    log.error("AbstractAsyncExecuteTask1执行异常：", e);
                    transactionManager.rollback(status);
                }
            } else {
                try {
                    doRun();
                } catch (Exception e) {
                    log.error("AbstractAsyncExecuteTask2执行异常", e);
                }
            }
        }

        if (!runComplete()) {
            log.warn("runComplete 异常....");
        }
    }

    /**
     * true 有数据
     * @return
     */
    protected synchronized  boolean hasData() {
        return dataList.size() > 0;
    }

    /**
     * 获取头部数据
     * @return
     */
    protected synchronized AsyncTask getHeaderData() {
        if (hasData()) {
            AsyncTask asyncTask = dataList.get(0);
            dataList.remove(0);
            return asyncTask;
        }

        return null;
    }

    /**
     * 获取所有数据
     * @return
     */
    protected synchronized List<AsyncTask> getAllData() {
        List<AsyncTask> resList = null;
        if (hasData()) {
            resList = new ArrayList<>(dataList.size());
            for (int i=0; i<dataList.size(); i++) {
                resList.add(i, dataList.get(i));
            }
            dataList.clear();
        }
        return resList;
    }

    protected int getStatus() {
        return status;
    }

    protected void setStatus(int status) {
        this.status = status;
    }

    /**
     * true 正在运行
     * @return
     */
    public boolean isRun() {
        return workerCount.get() > 0;
//        return StatusEnum.apply(getStatus()) == StatusEnum.Running;
    }

    /**
     * 执行任务
     */
    abstract void doRun();

    protected boolean beforeRun() {
//        setStatus(StatusEnum.Running.getValue());
        workerCount.getAndIncrement();
        return true;
    }

    protected boolean runComplete() {
//        setStatus(StatusEnum.UnRunning.getValue());
        workerCount.decrementAndGet();
        return true;
    }
}
