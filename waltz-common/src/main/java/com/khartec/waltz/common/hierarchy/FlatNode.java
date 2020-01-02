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

import java.util.Optional;


public class FlatNode<T, K> {

    private final K id;
    private final Optional<K> parentId;
    private final T data;


    public FlatNode(K id, Optional<K> parentId, T data) {
        this.id = id;
        this.parentId = parentId;
        this.data = data;
    }


    public Optional<K> getParentId() {
        return parentId;
    }


    public T getData() {
        return data;
    }


    public K getId() {
        return id;
    }

}
