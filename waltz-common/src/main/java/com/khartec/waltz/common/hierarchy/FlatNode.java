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
