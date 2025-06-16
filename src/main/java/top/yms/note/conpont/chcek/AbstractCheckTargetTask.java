package top.yms.note.conpont.chcek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import top.yms.note.entity.CheckTarget;
import top.yms.note.entity.CheckTargetRecord;
import top.yms.note.enums.CheckTargetStatus;
import top.yms.note.mapper.CheckTargetMapper;
import top.yms.note.mapper.CheckTargetRecordMapper;
import top.yms.note.utils.DateUtil;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class AbstractCheckTargetTask implements CheckTargetTask {

    private final static Logger log = LoggerFactory.getLogger(AbstractCheckTargetTask.class);

    @Resource
    protected CheckTargetMapper checkTargetMapper;

    @Resource
    protected CheckTargetRecordMapper checkTargetRecordMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    public boolean support(String name) {
        return false;
    }

    /**
     * 是否需要事务
     * <p>true 需要； false 不需要</p>
     * @return
     */
    protected boolean needTx() {
        return false;
    }

    public void excTask(CheckTarget checkTarget) {
        log.debug("start check task at {}", LocalDateTime.now());
//        log.info("start checkTarget: {}", JSON.toJSONString(checkTarget, JSONWriter.Feature.PrettyFormat));
        if (needTx()) {
            log.debug("------------Check Task 事务模式执行------------------");
            // 定义事务属性
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 可根据需要设置传播行为
            def.setTimeout(120); //设置120秒超时
            // 获取事务状态
            TransactionStatus status = transactionManager.getTransaction(def);
            try {
                doExcTask(checkTarget);
                transactionManager.commit(status);
            } catch (Throwable e) {
                log.error("excTask 【事务模式】 执行异常：", e);
                transactionManager.rollback(status);
                throwException(checkTarget);
            }
        } else {
            log.debug("------------Check Task [无]事务模式执行------------------");
            try {
                doExcTask(checkTarget);
            } catch (Exception e) {
                log.error("excTask 【无事务模式】 执行异常", e);
                throwException(checkTarget);
            }
        }
    }

    /**
     * 当发生异常时，回调子类
     * @param currentData
     */
    protected void throwException(Object currentData) {

    }

    private void doExcTask(CheckTarget checkTarget) {
        if (!beforeExc(checkTarget)) return;
        doCheckTask(checkTarget);
        afterExc(checkTarget);
    }

    /**
     * 执行前做些事情
     * @param checkTarget
     * @return
     */
    boolean beforeExc(CheckTarget checkTarget) {
        String excDate = checkTarget.getExcDate();
        String nowDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtil.yyy_MM_dd));
        log.info("excDate = {} , nowDate={}", excDate, nowDateStr);
        if (!excDate.equals(nowDateStr)) {
            return false;
        }
        String status = checkTarget.getStatus();
        if (!CheckTargetStatus.UN_RUN.getValue().equals(status)) {
            return false;
        }
        //update
        checkTarget.setStatus(CheckTargetStatus.RUNNING.getValue());
        //插入执行记录
        CheckTargetRecord checkTargetRecord = new CheckTargetRecord();
        BeanUtils.copyProperties(checkTarget, checkTargetRecord);
        checkTargetRecord.setId(null);
        checkTargetRecord.setCheckId(checkTarget.getId());
        checkTargetRecord.setExcTime(new Date());
        checkTargetRecord.setCreateTime(new Date());
        checkTargetRecordMapper.insertSelective(checkTargetRecord);
        //ret
        return true;
    }

    /**
     * 子类实现该方法：用于具体任务执行
     * @param checkTarget
     */
    abstract void doCheckTask(CheckTarget checkTarget);

    /**
     * 后置执行
     * @param checkTarget
     */
    void afterExc(CheckTarget checkTarget) {
        checkTarget.setStatus(CheckTargetStatus.COMPLETED.getValue());
        //插入执行记录
        CheckTargetRecord checkTargetRecord = new CheckTargetRecord();
        BeanUtils.copyProperties(checkTarget, checkTargetRecord);
        checkTargetRecord.setId(null);
        checkTargetRecord.setCheckId(checkTarget.getId());
        checkTargetRecord.setExcTime(new Date());
        checkTargetRecord.setCreateTime(new Date());
        checkTargetRecordMapper.insertSelective(checkTargetRecord);
        //生成下一次执行记录
        String excDateStr = checkTarget.getExcDate();
        LocalDate excDate = LocalDate.parse(excDateStr);
        Integer period = checkTarget.getPeriod();
        String nextExcDateStr = excDate.plusDays(period).format(DateTimeFormatter.ofPattern(DateUtil.yyy_MM_dd));
        log.info("name={}, nextExcDate={}", checkTarget.getName(), nextExcDateStr);
        checkTarget.setExcDate(nextExcDateStr);
        checkTarget.setStatus(CheckTargetStatus.UN_RUN.getValue());
        checkTargetMapper.updateByPrimaryKeySelective(checkTarget);
        //执行完成
    }
}
