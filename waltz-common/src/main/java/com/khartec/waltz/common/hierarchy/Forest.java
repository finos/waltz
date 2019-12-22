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
