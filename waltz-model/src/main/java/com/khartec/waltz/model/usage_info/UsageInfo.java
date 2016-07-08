package com.khartec.waltz.model.usage_info;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableUsageInfo.class)
@JsonDeserialize(as = ImmutableUsageInfo.class)
public abstract class UsageInfo {

    public abstract UsageKind kind();
    public abstract boolean isSelected();

    @Value.Default
    public String description() {
        return "";
    }

}
