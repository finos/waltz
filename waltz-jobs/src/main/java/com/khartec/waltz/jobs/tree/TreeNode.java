package com.khartec.waltz.jobs.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dwatkins on 27/06/2016.
 */
public class TreeNode {

    public TreeNode parent;
    public String data;
    public int leftIdx;
    public int rightIdx;
    public List<TreeNode> children = new LinkedList<>();

    public TreeNode(TreeNode parent, String data) {
        this.parent = parent;
        this.data = data;

        if (parent != null) {
            this.parent.children.add(this);
        }
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "parent=" + parent +
                ", data='" + data + '\'' +
                ", leftIdx=" + leftIdx +
                ", rightIdx=" + rightIdx +
                ", children=" + children.size() +
                '}';
    }
}
