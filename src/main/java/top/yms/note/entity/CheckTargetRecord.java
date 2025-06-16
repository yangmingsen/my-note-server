package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_check_target_record
 */
public class CheckTargetRecord {
    /**
     */
    private Long id;

    /**
     * check_target id
     */
    private Long checkId;

    /**
     */
    private String name;

    /**
     */
    private String status;

    /**
     * 执行时间
     */
    private Date excTime;

    /**
     * 创建时间
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCheckId() {
        return checkId;
    }

    public void setCheckId(Long checkId) {
        this.checkId = checkId;
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

    public Date getExcTime() {
        return excTime;
    }

    public void setExcTime(Date excTime) {
        this.excTime = excTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}