package org.finos.waltz.model.physical_flow;

import org.finos.waltz.model.Wrapped;
import org.finos.waltz.model.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@Wrapped
public abstract class _CriticalityValue extends Wrapper<String> {

    public static CriticalityValue UNKNOWN = CriticalityValue.of("UNKNOWN");

}
