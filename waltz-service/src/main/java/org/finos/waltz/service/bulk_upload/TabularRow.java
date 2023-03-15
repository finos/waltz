package org.finos.waltz.service.bulk_upload;

import org.immutables.value.Value;

@Value.Immutable
public abstract class TabularRow {

    public abstract int rowNumber();

    public abstract String[] values();


}
