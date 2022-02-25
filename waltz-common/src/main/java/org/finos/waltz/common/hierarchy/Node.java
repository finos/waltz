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

package org.finos.waltz.common.hierarchy;

import org.finos.waltz.common.Checks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;


public class Node<T, K> {

    private final K id;
    private final T data;
    private final Set<Node<T, K>> children = new HashSet<>();

    private Node<T, K> parent = null;


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


    public Node<T, K> addChild(Node<T, K> childNode) {
        Checks.checkNotNull(childNode, "childNode must not be null");
        children.add(childNode);
        return this;
    }


    public Node<T, K> setParent(Node<T, K> parent) {
        this.parent = parent;
        return this;
    }


    public Node<T, K> getParent() {
        return parent;
    }


    @Override
    public String toString() {
        return format(
                "Node{id=%s, #children=%d}",
                id,
                children.size());
    }
}
