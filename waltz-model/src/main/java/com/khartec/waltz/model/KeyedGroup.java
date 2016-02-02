package com.khartec.waltz.model;

/**
 * Created by dwatkins on 02/02/2016.
 */

import java.util.List;

public abstract class KeyedGroup<K, V> {

    public abstract K key();
    public abstract List<V> values();
}
