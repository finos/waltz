package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowUploadCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowUploadCommand.class)
public abstract class PhysicalFlowUploadCommand implements
        Command,
        DescriptionProvider {

    // logical flow
    public abstract String source();
    public abstract String target();

    // spec
    public abstract String owner();
    public abstract String name();
    public abstract String format();

    @Nullable
    public abstract String specDescription();

    @Nullable
    public abstract String specExternalId();

    // flow attributes
    public abstract String basisOffset();
    public abstract String criticality();
    public abstract String description();

    @Nullable
    public abstract String externalId();

    public abstract String frequency();
    public abstract String transport();

    public abstract String dataType();
}
