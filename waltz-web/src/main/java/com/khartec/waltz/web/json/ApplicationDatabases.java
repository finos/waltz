package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.database.Database;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationDatabases.class)
@JsonDeserialize(as = ImmutableApplicationDatabases.class)
public abstract class ApplicationDatabases {

    public abstract Long applicationId();
    public abstract List<Database> databases();

}
