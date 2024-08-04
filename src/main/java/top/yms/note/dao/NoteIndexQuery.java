package top.yms.note.dao;

import org.apache.commons.lang3.StringUtils;
import top.yms.note.entity.NoteIndexExample;

/**
 * Created by yangmingsen on 2024/4/5.
 */
public class NoteIndexQuery {
    private Long userId;
    private Long id;
    private Long parentId;
    private String name;

    /**
     * 存储位置(mysql,mongo)
     */
    private String storeSite;

    /**
     * 可能是t_note_file表的t_file_id
     */
    private String siteId;
    /**
     * true 删除
     * false 非删除
     */
    private boolean isDel = false;
    /**
     *  1.查找所有(含文件和目录);
     *  2.只查目录
     */
    private int filter;


    private NoteIndexExample example;

    public NoteIndexExample example() {
        if (example != null) return example;

        NoteIndexExample example = new NoteIndexExample();
        NoteIndexExample.Criteria criteria = example.createCriteria();

        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (id != null) {
            criteria.andIdEqualTo(id);
        }
        if (parentId != null) {
            criteria.andParentIdEqualTo(parentId);
        }
        if (StringUtils.isNotBlank(name)) {
            criteria.andNameLike(name);
        }
        if (StringUtils.isNotBlank(siteId)) {
            criteria.andSiteIdEqualTo(siteId);
        }
        if (StringUtils.isNotBlank(storeSite)) {
            criteria.andStoreSiteEqualTo(storeSite);
        }
        if (isDel) {
            criteria.andDelEqualTo("1");
        } else {
            criteria.andDelEqualTo("0");
        }
        if (filter == 2) {
            criteria.andIsileEqualTo("0");
        }

        return example;
    }

    public static class Builder {
        private NoteIndexQuery query;
        private Builder() {}
        public static Builder build() {
            Builder builder = new Builder();
            builder.query = new NoteIndexQuery();
            return builder;
        }

        public Builder uid(Long id){
            query.setUserId(id);
            return this;
        }

        public Builder nid(Long id) {
            query.setId(id);
            return this;
        }

        public Builder searchName(String name) {
            query.name= name;
            return this;
        }

        public Builder del(boolean f) {
            query.setDel(f);
            return this;
        }
        public Builder filter(int f) {
            query.setFilter(f);
            return this;
        }
        public Builder parentId(Long parentId) {
            query.setParentId(parentId);
            return this;
        }
        public Builder siteId(String siteId) {
            query.siteId= siteId;
            return this;
        }
        public Builder storeSite(String storeSite) {
            query.storeSite= storeSite;
            return this;
        }


        public NoteIndexQuery get() {
            return query;
        }
    }

    public String getStoreSite() {
        return storeSite;
    }

    public void setStoreSite(String storeSite) {
        this.storeSite = storeSite;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long nodeId) {
        this.id = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDel() {
        return isDel;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "NoteIndexQuery{" +
                "userId=" + userId +
                ", Id=" + id +
                ", name='" + name + '\'' +
                ", isDel=" + isDel +
                ", filter=" + filter +
                '}';
    }
}
