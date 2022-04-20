
package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInstance;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramInstance;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramInstanceRecord;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AggregateOverlayDiagramInstanceDao {

    private static final RecordMapper<? super Record, ? extends AggregateOverlayDiagramInstance> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramInstanceRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE);
        return ImmutableAggregateOverlayDiagramInstance.builder()
                .id(record.getId())
                .diagramId(record.getDiagramId())
                .parentEntityReference(mkRef(EntityKind.valueOf(record.getParentEntityKind()), record.getParentEntityId()))
                .svg(record.getSvg())
                .name(record.getName())
                .description(record.getDescription())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    protected final DSLContext dsl;


    @Autowired
    public AggregateOverlayDiagramInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public AggregateOverlayDiagramInstance getById(Long instanceId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .where(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.ID.eq(instanceId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<AggregateOverlayDiagramInstance> findByDiagramId(Long diagramId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .where(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }

}
