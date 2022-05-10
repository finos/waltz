package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.aggregate_overlay_diagram.*;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramCalloutRecord;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_CALLOUT;

@Repository
public class AggregateOverlayDiagramCalloutDao {


    private static final RecordMapper<? super Record, ? extends AggregateOverlayDiagramCallout> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramCalloutRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT);
        return ImmutableAggregateOverlayDiagramCallout.builder()
                .id(record.getId())
                .title(record.getTitle())
                .diagramInstanceId(record.getDiagramInstanceId())
                .cellExternalId(record.getCellExternalId())
                .content(record.getContent())
                .startColor(record.getStartColor())
                .endColor(record.getEndColor())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public AggregateOverlayDiagramCalloutDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public Set<AggregateOverlayDiagramCallout> findByDiagramInstanceId(Long diagramInstanceId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.DIAGRAM_INSTANCE_ID.eq(diagramInstanceId))
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }


    public int create(DiagramCalloutCreateCommand createCommand) {

        AggregateOverlayDiagramCalloutRecord record = dsl.newRecord(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT);
        record.setDiagramInstanceId(createCommand.instanceId());
        record.setCellExternalId(createCommand.cellExternalId());
        record.setTitle(createCommand.title());
        record.setContent(createCommand.content());
        record.setStartColor(createCommand.startColor());
        record.setEndColor(createCommand.endColor());

        return dsl
                .insertInto(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT)
                .set(record)
                .execute();
    }


    public Integer update(AggregateOverlayDiagramCallout callout) {
        return dsl
                .update(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT)
                .set(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.TITLE, callout.title())
                .set(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.CONTENT, callout.content())
                .set(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.START_COLOR, callout.startColor())
                .set(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.END_COLOR, callout.endColor())
                .where(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.ID.eq(callout.id().get()))
                .execute();
    }


    public Integer delete(long calloutId) {
        return dsl
                .deleteFrom(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CALLOUT.ID.eq(calloutId))
                .execute();
    }
}
