package com.khartec.waltz.model;

import java.util.List;

public abstract class KeyedGroup<K, V> {

    public abstract K key();
    public abstract List<V> values();
}
