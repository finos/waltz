package com.khartec.waltz.model.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDatabase.class)
@JsonDeserialize(as = ImmutableDatabase.class)
public abstract class Database implements ProvenanceProvider, ExternalIdProvider {

    public abstract String databaseName();
    public abstract String instanceName();
    public abstract String environment();

    public abstract String dbmsName();
    public abstract String dbmsVersion();
    public abstract String dbmsVendor();

}
