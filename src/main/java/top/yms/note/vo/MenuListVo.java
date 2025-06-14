package top.yms.note.vo;

import top.yms.note.entity.NoteMeta;

import java.util.List;

/**
 * Created by yangmingsen on 2024/4/4.
 */
public class MenuListVo {
    //目录列表
    List<NoteMeta> menuList;
    //文件列表
    List<NoteMeta> noteContentMenuList;

    public List<NoteMeta> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<NoteMeta> menuList) {
        this.menuList = menuList;
    }

    public List<NoteMeta> getNoteContentMenuList() {
        return noteContentMenuList;
    }

    public void setNoteContentMenuList(List<NoteMeta> noteContentMenuList) {
        this.noteContentMenuList = noteContentMenuList;
    }
}
