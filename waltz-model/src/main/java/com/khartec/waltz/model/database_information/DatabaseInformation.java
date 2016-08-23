package com.khartec.waltz.model.database_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.AssetCodeProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@JsonSerialize(as = ImmutableDatabaseInformation.class)
@JsonDeserialize(as = ImmutableDatabaseInformation.class)
public abstract class DatabaseInformation implements
        AssetCodeProvider,
        ProvenanceProvider,
        ExternalIdProvider {

    public abstract String databaseName();
    public abstract String instanceName();
    public abstract String environment();

    public abstract String dbmsName();
    public abstract String dbmsVersion();
    public abstract String dbmsVendor();

    @Nullable
    public abstract Date endOfLifeDate();

}
