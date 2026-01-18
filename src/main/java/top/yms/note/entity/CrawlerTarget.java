package top.yms.note.entity;

public class CrawlerTarget {

    private Long id;

    private String url;

    private String condition;

    /**
     * 1-可以进行爬虫，0-不可进行爬虫
     */
    private String open;

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
        this.url = url;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }
}
