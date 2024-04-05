package top.yms.note.vo;

import top.yms.note.entity.NoteIndex;

import java.util.List;

/**
 * Created by yangmingsen on 2024/4/4.
 */
public class MenuListVo {
    //目录列表
    List<NoteIndex> menuList;
    //文件列表
    List<NoteIndex> noteContentMenuList;

    public List<NoteIndex> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<NoteIndex> menuList) {
        this.menuList = menuList;
    }

    public List<NoteIndex> getNoteContentMenuList() {
        return noteContentMenuList;
    }

    public void setNoteContentMenuList(List<NoteIndex> noteContentMenuList) {
        this.noteContentMenuList = noteContentMenuList;
    }
}
