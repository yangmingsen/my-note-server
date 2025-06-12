package top.yms.note.conpont.task;

import com.alibaba.fastjson2.JSONObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import top.yms.note.comm.NoteConstants;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.msgcd.NoteSystemErrorCode;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    protected MongoTemplate mongoTemplate;

    @Override
    public synchronized void addTask(AsyncTask task) {
        dataList.add(task);
        log.debug("当前任务{}, 持有任务数: {}", this, getDataSize());
        //save task to db
        saveTask(task);
        //user custom
        afterAddTask(task);
    }

    /**
     * save task to db
     * @param task
     */
    protected void saveTask(AsyncTask task) {
        JSONObject taskInfo = JSONObject.from(task);
        Document newDoc = Document.parse(taskInfo.toString());
        mongoTemplate.save(newDoc, NoteConstants.taskInfoMessage);
    }

    protected void afterAddTask(AsyncTask task) {    }

    /**
     * 是否需要事务. true 需要
     * @return
     */
    abstract boolean needTx() ;

    @Override
    public void run() {
        log.debug("start task at {}", LocalDateTime.now());
        Object currentData = null;
        try {
            currentData = getCurrentNeedHandleData();
            if (!beforeRun(currentData)) {
                log.warn("beforeRun 异常....");
            }
            if (hasData()) {
                if (needTx()) {
                    log.debug("------------------------需要事务执行------------------");
                    // 定义事务属性
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 可根据需要设置传播行为
                    def.setTimeout(120); //设置30秒超时
                    // 获取事务状态
                    TransactionStatus status = transactionManager.getTransaction(def);
                    try {
                        doRun(currentData);
                        transactionManager.commit(status);
                    } catch (Throwable e) {
                        log.error("AbstractAsyncExecuteTask 【事务模式】 执行异常：", e);
                        transactionManager.rollback(status);
                        throwException(currentData);
                        throw e;
                    }
                } else {
                    log.debug("-----------------------无需事务执行-------------------");
                    try {
                        doRun(currentData);
                    } catch (Exception e) {
                        log.error("AbstractAsyncExecuteTask 【无事务模式】 执行异常", e);
                        throwException(currentData);
                        throw e;
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Note Task execute error", t);
            throw new NoteSystemException(NoteSystemErrorCode.E_400009);
        } finally {
            if (!runComplete(currentData)) {
                log.warn("runComplete 异常....");
            }
        }

    }

    /**
     * 给与子类一个机会获取当前正在处理的数据
     * @return
     */
    protected Object getCurrentNeedHandleData() throws Exception{
        return null;
    }

    /**
     * 当发生异常时，回调子类
     * @param currentData
     */
    protected void throwException(Object currentData) {

    }

    /**
     * true 有数据
     * @return
     */
    protected synchronized  boolean hasData() {
        return dataList.size() > 0;
    }

    protected synchronized  int getDataSize() {
        return dataList.size();
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
    abstract void doRun(Object data);

    protected Object getCurrentNeedHandleData(Object obj) {
        return getAllData();
    }

    protected boolean beforeRun(Object obj) {
//        setStatus(StatusEnum.Running.getValue());
        workerCount.getAndIncrement();
        return true;
    }

    protected boolean runComplete(Object obj) {
//        setStatus(StatusEnum.UnRunning.getValue());
        workerCount.decrementAndGet();
        return true;
    }
}
