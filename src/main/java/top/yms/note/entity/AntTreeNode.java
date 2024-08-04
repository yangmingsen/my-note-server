package top.yms.note.entity;

import java.util.List;

/**
 * Created by yangmingsen on 2024/8/3.
 */
public class AntTreeNode {
    private String title;
    private String key;
    private List<AntTreeNode> children;

    public AntTreeNode(String title, String key, List<AntTreeNode> children) {
        this.title = title;
        this.key = key;
        this.children = children;
    }

    public AntTreeNode() {    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<AntTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<AntTreeNode> children) {
        this.children = children;
    }
}
