package com.khartec.waltz.model.performance_metric.pack;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.checkpoint.Checkpoint;
import org.immutables.value.Value;

import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutableMetricPack.class)
@JsonDeserialize(as = ImmutableMetricPack.class)
public abstract class MetricPack implements
        IdProvider,
        NameProvider,
        DescriptionProvider {

    public abstract List<Checkpoint> checkpoints();
    public abstract List<MetricPackItem> items();

    public abstract List<EntityReference> relatedReferences();

}
