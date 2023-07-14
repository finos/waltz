
package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramInstance;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramInstance;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramInstanceCreateCommand;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramInstanceRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE;

@Repository
public class AggregateOverlayDiagramInstanceDao {


    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.PARENT_ENTITY_ID,
                    AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.PARENT_ENTITY_KIND,
                    newArrayList(
                            EntityKind.ORG_UNIT,
                            EntityKind.APP_GROUP,
                            EntityKind.MEASURABLE,
                            EntityKind.PERSON
                    ))
            .as("entity_name");

    private static final RecordMapper<? super Record, ? extends AggregateOverlayDiagramInstance> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramInstanceRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE);

        EntityReference parentEntityReference = mkRef(
                EntityKind.valueOf(record.getParentEntityKind()),
                record.getParentEntityId(),
                r.get(ENTITY_NAME_FIELD));

        return ImmutableAggregateOverlayDiagramInstance.builder()
                .id(record.getId())
                .diagramId(record.getDiagramId())
                .parentEntityReference(parentEntityReference)
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


    public Set<AggregateOverlayDiagramInstance> findAll() {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }


    public AggregateOverlayDiagramInstance getById(Long instanceId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .where(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.ID.eq(instanceId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<AggregateOverlayDiagramInstance> findByDiagramId(Long diagramId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .where(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }


    public int createInstance(OverlayDiagramInstanceCreateCommand createCommand,
                              String username) {

        AggregateOverlayDiagramInstanceRecord record = dsl.newRecord(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE);

        record.setDiagramId(createCommand.diagramId());
        record.setName(createCommand.name());
        record.setDescription(createCommand.description());
        record.setParentEntityKind(createCommand.parentEntityReference().kind().name());
        record.setParentEntityId(createCommand.parentEntityReference().id());
        record.setSvg(createCommand.svg());
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy(username);
        record.setProvenance("waltz");

        return dsl
                .insertInto(AGGREGATE_OVERLAY_DIAGRAM_INSTANCE)
                .set(record)
                .execute();

    }

}
