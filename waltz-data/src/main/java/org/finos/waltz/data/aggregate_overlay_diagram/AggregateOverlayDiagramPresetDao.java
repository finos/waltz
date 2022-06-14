package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramPreset;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramPreset;
import org.finos.waltz.model.aggregate_overlay_diagram.OverlayDiagramPresetCreateCommand;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramPresetRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_PRESET;

@Repository
public class AggregateOverlayDiagramPresetDao {

    protected static final RecordMapper<? super Record, ? extends AggregateOverlayDiagramPreset> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramPresetRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM_PRESET);
        return ImmutableAggregateOverlayDiagramPreset.builder()
                .id(record.getId())
                .name(record.getName())
                .diagramId(record.getDiagramId())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .overlayConfig(record.getOverlayConfig())
                .filterConfig(record.getFilterConfig())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public AggregateOverlayDiagramPresetDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<AggregateOverlayDiagramPreset> findPresetsForDiagram(Long diagramId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_PRESET.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM_PRESET)
                .where(AGGREGATE_OVERLAY_DIAGRAM_PRESET.DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }

    public int create(OverlayDiagramPresetCreateCommand createCommand, String username) {

        AggregateOverlayDiagramPresetRecord record = dsl.newRecord(AGGREGATE_OVERLAY_DIAGRAM_PRESET);

        record.setDiagramId(createCommand.diagramId());
        record.setName(createCommand.name());
        record.setDescription(createCommand.description());
        record.setExternalId(createCommand.externalId());
        record.setOverlayConfig(createCommand.overlayConfig());
        record.setFilterConfig(createCommand.filterConfig());
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy(username);
        record.setProvenance("waltz");

        return dsl
                .insertInto(AGGREGATE_OVERLAY_DIAGRAM_PRESET)
                .set(record)
                .execute();
    }

}
