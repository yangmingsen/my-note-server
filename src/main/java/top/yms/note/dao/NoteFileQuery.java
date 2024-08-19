package top.yms.note.dao;

import org.apache.commons.lang3.StringUtils;
import top.yms.note.entity.NoteFileExample;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public class NoteFileQuery {
    /**
     * pk
     */
    private Long id;

    /**
     * 文件id(mongo数据库)
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件类型(txt,jpg等)
     */
    private String type;

    /**
     * 文件大小(byte)
     */
    private Long size;

    /**
     * 所属用户id
     */
    private Long userId;

    private Long noteRefId;


    private NoteFileExample example;


    public NoteFileExample example() {
        if (example != null) return example;

        NoteFileExample example = new NoteFileExample();
        NoteFileExample.Criteria criteria = example.createCriteria();
        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (id != null) {
            criteria.andIdEqualTo(id);
        }
        if (StringUtils.isNotBlank(fileId)) {
            criteria.andFileIdEqualTo(fileId);
        }
        if (StringUtils.isNotBlank(name)) {
            criteria.andNameLike(name);
        }
        if (StringUtils.isNotBlank(type)) {
            criteria.andTypeLike(type);
        }
        if (size != null) {
            criteria.andSizeEqualTo(size);
        }

        if (noteRefId != null) {
            criteria.andNoteRefEqualTo(noteRefId);
        }

        return example;
    }

    public static class Builder {
        private NoteFileQuery query;
        private Builder() {}
        public static NoteFileQuery.Builder build() {
            Builder builder = new Builder();
            builder.query = new NoteFileQuery();
            return builder;
        }

        public Builder fileId(String id){
            query.setFileId(id);
            return this;
        }
        public Builder name(String name){
            query.setName(name);
            return this;
        }
        public Builder type(String type){
            query.setType(type);
            return this;
        }
        public Builder size(long size){
            query.setSize(size);
            return this;
        }


        public Builder userid(Long id){
            query.setUserId(id);
            return this;
        }

        public Builder id(Long id) {
            query.setId(id);
            return this;
        }

        public Builder noteRefId(Long noteId) {
            query.setNoteRefId(noteId);
            return this;
        }


        public NoteFileQuery get() {
            return query;
        }
    }


    public Long getNoteRefId() {
        return noteRefId;
    }

    public void setNoteRefId(Long noteRefId) {
        this.noteRefId = noteRefId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
