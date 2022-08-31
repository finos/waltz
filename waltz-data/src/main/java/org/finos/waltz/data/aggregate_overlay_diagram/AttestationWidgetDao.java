package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AttestationEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.AttestationWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAttestationEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableAttestationWidgetDatum;
import org.finos.waltz.schema.tables.AttestationInstance;
import org.finos.waltz.schema.tables.AttestationRun;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadCellExtIdToAggregatedEntities;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.loadExpandedCellMappingsForDiagram;

@Repository
public class AttestationWidgetDao {

    private static final AttestationInstance att_i = AttestationInstance.ATTESTATION_INSTANCE;
    private static final AttestationRun att_r = AttestationRun.ATTESTATION_RUN;

    private final DSLContext dsl;


    @Autowired
    public AttestationWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<AttestationWidgetDatum> findWidgetData(long diagramId,
                                                      EntityKind attestedEntityKind,
                                                      Optional<Long> attestedEntityId,
                                                      Select<Record1<Long>> inScopeEntityIdSelector) {

        Set<Tuple2<String, EntityReference>> cellWithBackingEntities = loadExpandedCellMappingsForDiagram(dsl, diagramId);

        Map<String, Set<Long>> cellExtIdsToAggregatedEntities = loadCellExtIdToAggregatedEntities(
                dsl,
                cellWithBackingEntities,
                EntityKind.APPLICATION,
                inScopeEntityIdSelector,
                Optional.empty());

        SelectConditionStep<Record5<String, Long, Timestamp, String, Integer>> rawAttestationData = dsl
                .select(
                        att_i.PARENT_ENTITY_KIND.as("ref_k"),
                        att_i.PARENT_ENTITY_ID.as("ref_i"),
                        att_i.ATTESTED_AT.as("att_at"),
                        att_i.ATTESTED_BY.as("att_by"),
                        DSL.rowNumber().over(DSL
                                .partitionBy(
                                        att_i.PARENT_ENTITY_KIND,
                                        att_i.PARENT_ENTITY_ID)
                                .orderBy(att_i.ATTESTED_AT.desc())).as("latest"))
                .from(att_i)
                .innerJoin(att_r).on(att_i.ATTESTATION_RUN_ID.eq(att_r.ID))
                .where(att_i.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(att_i.PARENT_ENTITY_ID.in(inScopeEntityIdSelector))
                .and(att_r.ATTESTED_ENTITY_KIND.eq(attestedEntityKind.name())
                        .and(attestedEntityId
                                .map(att_r.ATTESTED_ENTITY_ID::eq)
                                .orElse(DSL.trueCondition())));

        SelectConditionStep<Record> latestAttestationData = dsl
                .select(rawAttestationData.fields())
                .from(rawAttestationData.asTable())
                .where(rawAttestationData.field("latest", Integer.class).eq(1));

        Map<Long, AttestationEntry> entityIdToAttestationInfo = latestAttestationData.fetchMap(
                r -> r.get("ref_i", Long.class),
                r -> ImmutableAttestationEntry.builder()
                        .attestedAt(toLocalDateTime(r.get("att_at", Timestamp.class)))
                        .attestedBy(r.get("att_by", String.class))
                        .appId(r.get("ref_i", Long.class))
                        .build());

        return cellExtIdsToAggregatedEntities
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<Long> entityIds = e.getValue();
                    Set<AttestationEntry> attestations = map(
                            entityIds,
                            id -> entityIdToAttestationInfo.getOrDefault(id, AttestationEntry.mkUnattestedEntry(id)));
                    return ImmutableAttestationWidgetDatum
                            .builder()
                            .cellExternalId(cellExtId)
                            .attestations(attestations)
                            .build();
                })
                .collect(toSet());
    }
}
