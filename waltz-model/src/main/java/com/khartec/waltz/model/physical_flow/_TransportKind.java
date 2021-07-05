package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.model.Wrapped;
import com.khartec.waltz.model.Wrapper;
import org.immutables.value.Value;

@Value.Immutable @Wrapped
public abstract class _TransportKind extends Wrapper<String> {

    public static TransportKind UNKNOWN = TransportKind.of("UNKNOWN");

}
