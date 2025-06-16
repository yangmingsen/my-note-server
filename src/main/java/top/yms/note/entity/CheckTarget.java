package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_check_target
 */
public class CheckTarget {
    /**
     */
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 状态
     * 1.未执行
     * 2.执行中
     * 3.执行完成
     */
    private String status;

    /**
     * 周期
     */
    private Integer period;

    /**
     * 执行日期
     */
    private String excDate;

    /**
     */
    private Date createTime;

    /**
     */
    private Date updateTime;

    /**
     * 描述
     */
    private String desc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getExcDate() {
        return excDate;
    }

    public void setExcDate(String excDate) {
        this.excDate = excDate == null ? null : excDate.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }
}