package top.yms.note.conpont.task;

import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;

import java.util.Date;

/**
 * 异步任务描述类
 */
public  class AsyncTask {
    private Long taskId;
    private String taskName;
    private Date createTime;


    /**
     * <p>任务执行方式： 1.立即执行  2.定时任务执行  3.调用者线程执行</p>
     * 注意： 2定时任务执行, 不需要设置, 因为执行类已经初始化设置过了, 见 UserConfigTask
     */
    private AsyncExcuteTypeEnum executeType;

    //任务类型
    private AsyncTaskEnum type;

    private Long userId;

    /**
     * 任务元数据
     */
    private Object taskInfo;


    public static class Builder {
        private AsyncTask asyncTask;
        private Builder() {}
        public static Builder build() {
            Builder builder = new Builder();
            builder.asyncTask = new AsyncTask();
            return builder;
        }

        public Builder taskId(Long taskId) {
            asyncTask.setTaskId(taskId);
            return this;
        }

        public Builder taskName(String taskName) {
            asyncTask.setTaskName(taskName);
            return this;
        }

        public Builder createTime(Date createTime) {
            asyncTask.setCreateTime(createTime);
            return this;
        }

        public Builder executeType(AsyncExcuteTypeEnum executeType) {
            asyncTask.setExecuteType(executeType);
            return this;
        }

        public Builder type(AsyncTaskEnum type) {
            asyncTask.setType(type);
            return this;
        }

        public Builder taskInfo(Object taskInfo) {
            asyncTask.setTaskInfo(taskInfo);
            return this;
        }

        public Builder userId(Long userId ) {
            asyncTask.setUserId(userId);
            return this;
        }

        public AsyncTask get() {
            return asyncTask;
        }
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AsyncExcuteTypeEnum getExecuteType() {
        return executeType;
    }

    public void setExecuteType(AsyncExcuteTypeEnum executeType) {
        this.executeType = executeType;
    }

    public AsyncTaskEnum getType() {
        return type;
    }

    public void setType(AsyncTaskEnum type) {
        this.type = type;
    }

    public Object getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(Object taskInfo) {
        this.taskInfo = taskInfo;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AsyncTask{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", createTime=" + createTime +
                ", executeType=" + executeType +
                ", type=" + type +
                ", taskInfo=" + taskInfo +
                '}';
    }
}
