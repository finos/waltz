package org.finos.waltz.model.physical_flow;

import org.finos.waltz.model.Wrapped;
import org.finos.waltz.model.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@Wrapped
public abstract class _FrequencyKindValue extends Wrapper<String> {

    public static FrequencyKindValue UNKNOWN = FrequencyKindValue.of("UNKNOWN");

}
