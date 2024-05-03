package org.finos.waltz.model.flow_classification;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowClassification.class)
@JsonDeserialize(as = ImmutableFlowClassification.class)
public abstract class FlowClassification implements IdProvider, NameProvider, DescriptionProvider, EntityKindProvider {

    public abstract String code();
    public abstract String color();
    public abstract int position();
    public abstract boolean isCustom();
    public abstract boolean userSelectable();

    @Value.Default
    public FlowDirection direction() {
        return FlowDirection.INBOUND;
    }

    @Nullable
    public abstract String defaultMessage();

    @Value.Default
    public MessageSeverity messageSeverity() {
        return MessageSeverity.INFORMATION;
    }
    @Value.Default
    public EntityKind kind() {
        return EntityKind.FLOW_CLASSIFICATION;
    }

}

