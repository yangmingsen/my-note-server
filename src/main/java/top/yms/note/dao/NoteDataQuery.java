package top.yms.note.dao;

import top.yms.note.entity.NoteDataExample;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public class NoteDataQuery {
    /**
     */
    private Long id;

    /**
     */
    private Long userId;

    /**
     * 笔记内容
     */
    private String content;


    private NoteDataExample example;

    public NoteDataExample example() {
        if (example != null) return example;

        NoteDataExample example = new NoteDataExample();
        NoteDataExample.Criteria criteria = example.createCriteria();
        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (id != null) {
            criteria.andIdEqualTo(id);
        }

        return example;
    }


    public static class Builder {
        private NoteDataQuery query;
        private Builder() {}
        public static Builder build() {
            Builder builder = new Builder();
            builder.query = new NoteDataQuery();
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


        public NoteDataQuery get() {
            return query;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
