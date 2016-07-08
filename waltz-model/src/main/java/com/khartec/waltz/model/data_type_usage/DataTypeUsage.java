package com.khartec.waltz.model.data_type_usage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.usage_info.UsageInfo;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataTypeUsage.class)
@JsonDeserialize(as = ImmutableDataTypeUsage.class)
public abstract class DataTypeUsage implements
        ProvenanceProvider {

    public abstract EntityReference entityReference();
    public abstract String dataTypeCode();
    public abstract UsageInfo usage();

}
