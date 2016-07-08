package com.khartec.waltz.model.system;


import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class SystemChangeSet<V, K> {

    public abstract List<V> updates();
    public abstract List<K> deletes();
    public abstract List<V> inserts();
}
