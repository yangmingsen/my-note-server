package top.yms.note.entity;

import java.util.List;

/**
 * Created by yangmingsen on 2024/3/30.
 */
public class NoteTree {
    private Long id;
    private Long parentId;
    private String label;
    private List<NoteTree> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<NoteTree> getChildren() {
        return children;
    }

    public void setChildren(List<NoteTree> children) {
        this.children = children;
    }
}
