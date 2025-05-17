package top.yms.note.entity;

import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_note_user
 */
public class NoteUser {
    /**
     */
    private Long id;

    /**
     */
    private String username;

    /**
     */
    private String password;

    /**
     */
    private String sex;

    /**
     * 自我描述
     */
    private String desc;

    /**
     * 名字
     */
    private String nickName;

    /**
     * 用户头像地址
     */
    private String avtar;

    /**
     */
    private Date createTime;

    /**
     * 是否删除(1删除，0否)
     */
    private String del;

    /**
     */
    private Date updateTime;

    private Long noteRootTreeId;

    public Long getNoteRootTreeId() {
        return noteRootTreeId;
    }

    public void setNoteRootTreeId(Long noteRootTreeId) {
        this.noteRootTreeId = noteRootTreeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public String getAvtar() {
        return avtar;
    }

    public void setAvtar(String avtar) {
        this.avtar = avtar == null ? null : avtar.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDel() {
        return del;
    }

    public void setDel(String del) {
        this.del = del == null ? null : del.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}