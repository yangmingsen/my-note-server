package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_system_config
 */
public class SystemConfig {
    /**
     */
    private Long id;

    /**
     */
    private String conigKey;

    /**
     */
    private String conigValue;

    /**
     */
    private String desc;

    /**
     */
    private Date createTime;

    /**
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConigKey() {
        return conigKey;
    }

    public void setConigKey(String conigKey) {
        this.conigKey = conigKey == null ? null : conigKey.trim();
    }

    public String getConigValue() {
        return conigValue;
    }

    public void setConigValue(String conigValue) {
        this.conigValue = conigValue == null ? null : conigValue.trim();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
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

    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + id +
                ", conigKey='" + conigKey + '\'' +
                ", conigValue='" + conigValue + '\'' +
                ", desc='" + desc + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}