package org.finos.waltz.data.overlay_diagram;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.tables.JwsOverlayDiagramCellData.JWS_OVERLAY_DIAGRAM_CELL_DATA;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class OverlayDiagramDao {

    protected final DSLContext dsl;


    @Autowired
    public OverlayDiagramDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    protected Map<String, Set<Long>> fetchAndGroupAppIdsByCellId(Select<Record2<String, Long>> cellExtIdWithAppIdSelector) {
        return dsl
                .selectQuery(cellExtIdWithAppIdSelector)
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
     * @return a select statement returning a list of `{cellExtId, appId}` entries
     */
    protected Select<Record2<String, Long>> mkOverlayEntityCellApplicationSelector(long diagramId) {

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

        Set<Select<Record2<String, Long>>> stuffToUnion = dsl
                .select(JWS_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        JWS_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        JWS_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .from(JWS_OVERLAY_DIAGRAM_CELL_DATA)
                .where(JWS_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId))
                .fetchSet(r -> {
                    r.get(JWS_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND);

                    Select<Record1<Long>> appSelectorForRelatedEntity = applicationIdSelectorFactory.apply(mkOpts(readRef(
                            r,
                            JWS_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                            JWS_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)));

                    return DSL
                            .select(DSL.val(r.get(JWS_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID)).as("cell_ext_id"),
                                    appSelectorForRelatedEntity.field(0, Long.class))
                            .from(appSelectorForRelatedEntity);
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


    /**
     * Combines the cell and app id selector with the inScope (for this vantage point) app id selector.
     * Return only appIds which should appear on the diagram  (basically the intersection between the two selectors).
     */
    protected Select<Record1<Long>> mkAppIdSelectorForDiagram(Select<Record2<String, Long>> cellAndAppSelector,
                                                              Select<Record1<Long>> inScopeApplicationSelector) {
        return DSL
                .select(cellAndAppSelector.field(1, Long.class))
                .from(cellAndAppSelector)
                .where(dsl.renderInlined(cellAndAppSelector.field(1, Long.class).in(inScopeApplicationSelector)));
    }


}
