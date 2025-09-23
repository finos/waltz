package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.utils.ImmutableProposeFlowPermission;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableProposeFlowPermission.class)
@JsonDeserialize(as = ImmutableProposeFlowPermission.class)
public abstract class ProposeFlowPermission {
    public abstract Set<Operation> sourceApprover();

    public abstract Set<Operation> targetApprover();
}
