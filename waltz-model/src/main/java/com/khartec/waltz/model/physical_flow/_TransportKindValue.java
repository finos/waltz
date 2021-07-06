package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.model.Wrapped;
import com.khartec.waltz.model.Wrapper;
import org.immutables.value.Value;

@Value.Immutable @Wrapped
public abstract class _TransportKindValue extends Wrapper<String> {

    public static TransportKindValue UNKNOWN = TransportKindValue.of("UNKNOWN");

}
