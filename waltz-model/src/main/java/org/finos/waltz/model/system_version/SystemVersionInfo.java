package org.finos.waltz.model.system_version;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSystemVersionInfo.class)
public abstract class SystemVersionInfo {

    public abstract String packageVersion();
    public abstract List<String> databaseVersions();
}
