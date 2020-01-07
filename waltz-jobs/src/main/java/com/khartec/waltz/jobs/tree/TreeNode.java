/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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
