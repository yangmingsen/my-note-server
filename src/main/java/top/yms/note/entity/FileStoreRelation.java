package top.yms.note.entity;

import org.apache.commons.lang3.StringUtils;

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
     * note系统文件id(本次自定义的id)
     */
    private String noteFileId;

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

    public String getNoteFileId() {
        return noteFileId;
    }

    public void setNoteFileId(String noteFileId) {
        this.noteFileId = noteFileId;
    }

    public String getCacheKey() {
        StringBuilder tmpStr = new StringBuilder();
        if (id != null) {
            tmpStr.append(id);
        }
        if (StringUtils.isNotBlank(mongoFileId)) {
            tmpStr.append("#").append(mongoFileId);
        }
        if (StringUtils.isNotBlank(storageFileId)) {
            tmpStr.append("#").append(storageFileId);
        }
        if (StringUtils.isNotBlank(noteFileId)) {
            tmpStr.append("#").append(noteFileId);
        }
        return tmpStr.toString();
    }
}