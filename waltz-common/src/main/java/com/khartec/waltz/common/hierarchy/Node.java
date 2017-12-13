/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
