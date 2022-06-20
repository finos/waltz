package org.finos.waltz.model.physical_specification;

import org.finos.waltz.model.Wrapped;
import org.finos.waltz.model.Wrapper;
import org.immutables.value.Value;

@Value.Immutable
@Wrapped
public abstract class _DataFormatKindValue extends Wrapper<String> {

    public static DataFormatKindValue UNKNOWN = DataFormatKindValue.of("UNKNOWN");

}
