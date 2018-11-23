package com.khartec.waltz.data;

import com.khartec.waltz.model.EntityKind;
import org.immutables.value.Value;
import org.jooq.Record1;
import org.jooq.Select;

@Value.Immutable
public abstract class GenericSelector {
    public abstract Select<Record1<Long>> selector();
    public abstract EntityKind kind();
}
