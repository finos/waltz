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

package com.khartec.waltz.common.hierarchy;

import com.khartec.waltz.common.Checks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Node<T, K> {

    private final K id;
    private final T data;
    private Node<T, K> parent = null;
    private Set<Node<T, K>> children = new HashSet<>();


    public Node(K id, T data) {
        this.id = id;
        this.data = data;
    }


    public K getId() {
        return id;
    }


    public T getData() {
        return data;
    }


    public Set<Node<T, K>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public Node addChild(Node childNode) {
        Checks.checkNotNull(childNode, "childNode must not be null");
        children.add(childNode);
        return this;
    }


    public Node setParent(Node<T, K> parent) {
        this.parent = parent;
        return this;
    }


    public Node<T, K> getParent() {
        return parent;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Node{");
        sb.append("id=").append(id);
        sb.append(", #children=").append(children.size());
        sb.append('}');
        return sb.toString();
    }
}
