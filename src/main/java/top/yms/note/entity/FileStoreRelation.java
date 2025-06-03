package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_file_store_relation
 */
public class FileStoreRelation {
    /**
     * pk
     */
    private Long id;

    /**
     * mongo file id
     */
    private String mongoFileId;

    /**
     * file_storage_id
     */
    private String storageFileId;

    /**
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMongoFileId() {
        return mongoFileId;
    }

    public void setMongoFileId(String mongoFileId) {
        this.mongoFileId = mongoFileId == null ? null : mongoFileId.trim();
    }

    public String getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(String storageFileId) {
        this.storageFileId = storageFileId == null ? null : storageFileId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}