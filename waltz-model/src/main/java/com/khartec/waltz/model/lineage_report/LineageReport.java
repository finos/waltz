package com.khartec.waltz.model.lineage_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

/**
 * Created by dwatkins on 12/10/2016.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLineageReport.class)
@JsonDeserialize(as = ImmutableLineageReport.class)
public abstract class LineageReport implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider
{
    public abstract Long specificationId();
}
