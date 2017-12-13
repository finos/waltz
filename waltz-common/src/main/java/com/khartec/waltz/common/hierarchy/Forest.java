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

import java.util.Map;
import java.util.Set;


public class Forest<T, K> {

    private final Map<K, Node<T, K>> allNodes;
    private final Set<Node<T, K>> rootNodes;


    public Forest(Map<K, Node<T, K>> allNodes, Set<Node<T, K>> rootNodes) {
        this.allNodes = allNodes;
        this.rootNodes = rootNodes;
    }


    public Map<K, Node<T, K>> getAllNodes() {
        return allNodes;
    }


    public Set<Node<T, K>> getRootNodes() {
        return rootNodes;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Forest{");
        sb.append("#allNodes=").append(allNodes.size());
        sb.append(", rootNodes=").append(rootNodes);
        sb.append('}');
        return sb.toString();
    }
}
