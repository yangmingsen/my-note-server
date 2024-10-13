package top.yms.note.conpont.task;

import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingsen on 2024/10/13.
 *
 * 延迟执行任务类
 */
public class DelayExecuteAsyncTask extends AsyncTask{

    /**
     * 时间单位
     */
    private TimeUnit unit;

    /**
     * 延时时间
     */
    private long delay;

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }


    public static class Builder {
        private DelayExecuteAsyncTask delayAsyncTask;
        private Builder() {}
        public static Builder build() {
            Builder builder = new Builder();
            builder.delayAsyncTask = new DelayExecuteAsyncTask();
            return builder;
        }

        public Builder taskId(Long taskId) {
            delayAsyncTask.setTaskId(taskId);
            return this;
        }

        public Builder taskName(String taskName) {
            delayAsyncTask.setTaskName(taskName);
            return this;
        }

        public Builder createTime(Date createTime) {
            delayAsyncTask.setCreateTime(createTime);
            return this;
        }

        public Builder executeType(AsyncExcuteTypeEnum executeType) {
            delayAsyncTask.setExecuteType(executeType);
            return this;
        }

        public Builder type(AsyncTaskEnum type) {
            delayAsyncTask.setType(type);
            return this;
        }

        public Builder taskInfo(Object taskInfo) {
            delayAsyncTask.setTaskInfo(taskInfo);
            return this;
        }

        public Builder userId(Long userId ) {
            delayAsyncTask.setUserId(userId);
            return this;
        }

        public Builder timeUnit(TimeUnit timeUnit) {
            delayAsyncTask.setUnit(timeUnit);
            return this;
        }

        public Builder delay(Long delay ) {
            delayAsyncTask.setDelay(delay);
            return this;
        }

        public DelayExecuteAsyncTask get() {
            return delayAsyncTask;
        }
    }

}
