
package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagram;
import org.finos.waltz.schema.tables.records.AggregateOverlayDiagramRecord;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AggregateOverlayDiagramUtilities {

    protected static final RecordMapper<? super Record, ? extends AggregateOverlayDiagram> TO_DOMAIN_MAPPER = r -> {
        AggregateOverlayDiagramRecord record = r.into(AGGREGATE_OVERLAY_DIAGRAM);
        return ImmutableAggregateOverlayDiagram.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .svg(record.getSvg())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .aggregatedEntityKind(EntityKind.valueOf(record.getAggregateEntityKind()))
                .build();
    };


    protected static Map<String, Set<Long>> fetchAndGroupAppIdsByCellId(DSLContext dsl,
                                                                        Select<Record2<String, Long>> cellExtIdWithEntityIdSelector) {

        return dsl
                .selectQuery(cellExtIdWithEntityIdSelector)
                .fetchSet(r -> tuple(r.get(0, String.class), r.get(1, Long.class)))
                .stream()
                .collect(groupingBy(
                        k -> k.v1,
                        mapping(t -> t.v2, toSet())));
    }


    /**
     * Returns a selector which returns cell ext ids and app ids.
     * The result (when executed) would look like:
     *
     *   cellExtId | appId
     *   ---       | ---
     *   AFC       | 45
     *   AFC       | 975
     *   FINANCE   | 435
     *   FINANCE   | 45
     *
     * Note, apps id may appear twice if they are in multiple cells.
     * Note, all app relevant app ids are returned, this does not take into account the vantage point
     *
     * @param diagramId  diagram identifier
     * @param aggregatedEntityKind
     * @return a select statement returning a list of `{cellExtId, appId}` entries
     */
    protected static Select<Record2<String, Long>> mkOverlayEntityCellAggregateEntitySelector(DSLContext dsl,
                                                                                              long diagramId,
                                                                                              EntityKind aggregatedEntityKind) {

        GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

        Set<Select<Record2<String, Long>>> stuffToUnion = selectCellMappingsForDiagram(dsl, diagramId)
                .fetchSet(r -> {
                    r.get(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND);

                    IdSelectionOptions idSelectionOptions = mkOpts(readRef(
                            r,
                            AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                            AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID));

                    GenericSelector entityIdSelector = genericSelectorFactory.applyForKind(aggregatedEntityKind, idSelectionOptions);

                    return DSL
                            .select(DSL.val(r.get(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID)).as("cell_ext_id"),
                                    entityIdSelector.selector().field(0, Long.class))
                            .from(entityIdSelector.selector());
                });

        return stuffToUnion
                .stream()
                .reduce(null,
                        (acc, r) -> {
                            if (acc == null) {
                                return r;
                            } else {
                                return acc.union(r);
                            }
                        });
    }


    protected static SelectConditionStep<Record3<String, String, Long>> selectCellMappingsForDiagram(DSLContext dsl,
                                                                                                     long diagramId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .from(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId));
    }


    /**
     * Takes the maximal set of app ids that may appear on the diagram (derived by unioning the values of
     * the cellExtIdsToEntityIdsMap) and filters then by the apps associated to the vantage point (given via
     * the inScopeAggregatedEntitySelector).
     * <p>
     * Returns only appIds which should appear on the diagram  (basically the intersection between the
     * maximal set given diagram cell mappings and the vantage point selector).
     */
    protected static Set<Long> calcExactEntityIdsOnDiagram(DSLContext dsl,
                                                           Map<String, Set<Long>> cellExtIdsToEntityIdsMap,
                                                           Select<Record1<Long>> inScopeAggregatedEntitySelector) {
        Set<Long> maximalEntityIdsForCells = cellExtIdsToEntityIdsMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(toSet());

        return dsl
                .select(inScopeAggregatedEntitySelector.field(0, Long.class))
                .from(inScopeAggregatedEntitySelector)
                .where(inScopeAggregatedEntitySelector.field(0).in(maximalEntityIdsForCells))
                .fetchSet(0, Long.class);
    }

}
