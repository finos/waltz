package com.khartec.waltz.model.database_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Date;

import static com.khartec.waltz.model.EndOfLifeStatus.calculateEndOfLifeStatus;

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
    public abstract LifecycleStatus lifecycleStatus();


    @Nullable
    public abstract Date endOfLifeDate();


    @Value.Derived
    public EndOfLifeStatus endOfLifeStatus() {
        return calculateEndOfLifeStatus(endOfLifeDate());
    }

}
