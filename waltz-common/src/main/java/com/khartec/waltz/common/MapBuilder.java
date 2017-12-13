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

package com.khartec.waltz.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MapBuilder<K, V> {

    private Map<K,V> map = new HashMap<>();

    public MapBuilder<K,V> from(Map<K, V> m) {
        map.putAll(m);
        return this;
    }

    public MapBuilder<K,V> add(K k, V v) {
        map.put(k, v);
        return this;
    }

    public Map<K, V> build() {
        return Collections.unmodifiableMap(map);
    }
}
