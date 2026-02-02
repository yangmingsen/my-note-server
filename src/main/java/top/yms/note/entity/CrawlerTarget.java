package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_crawler_target
 */
public class CrawlerTarget {
    /**
     */
    private Long id;

    /**
     * 目标地址
     */
    private String url;

    /**
     * 条件
     */
    private String condition;

    /**
     * 1-打开，0-关闭
     */
    private String open;

    /**
     */
    private Date createTime;

    /**
     * 更新time
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition == null ? null : condition.trim();
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open == null ? null : open.trim();
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
}