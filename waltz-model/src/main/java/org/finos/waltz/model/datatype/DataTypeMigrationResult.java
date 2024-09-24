package org.finos.waltz.model.datatype;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableDataTypeMigrationResult.class)
public interface DataTypeMigrationResult {
    long usageCount();
    long classificationRuleCount();
    long logicalFlowDataTypeCount();
    long physicalSpecDataTypeCount();
    boolean dataTypeRemoved();
}
