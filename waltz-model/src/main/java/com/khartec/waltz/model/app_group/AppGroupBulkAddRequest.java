package com.khartec.waltz.model.app_group;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAppGroupBulkAddRequest.class)
@JsonDeserialize(as = ImmutableAppGroupBulkAddRequest.class)
public abstract class AppGroupBulkAddRequest {

    public abstract List<Long> applicationIds();
    public abstract List<String> unknownIdentifiers();

}
