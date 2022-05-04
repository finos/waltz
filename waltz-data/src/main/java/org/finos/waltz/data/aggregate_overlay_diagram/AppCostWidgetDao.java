package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.common.OptionalUtilities;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.*;

@Repository
public class AppCostWidgetDao {

    private final DSLContext dsl;


    @Autowired
    public AppCostWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    private Optional<Map<String, Set<Long>>> loadCellExtIdsToAppIdsMap(DSLContext dsl,
                                                                       long diagramId) {
        Select<Record2<String, Long>> cellExtIdWithAppIdSelector = mkOverlayEntityCellApplicationSelector(
                dsl,
                diagramId);

        if (cellExtIdWithAppIdSelector == null) {
            // no cell mapping data so short circuit and give no results
            return Optional.empty();
        }

        return Optional.of(fetchAndGroupAppIdsByCellId(
                dsl,
                cellExtIdWithAppIdSelector));

    }


    public Set<?> foo(long diagramId,
                      Select<Record1<Long>> inScopeApplicationSelector) {

        Optional<Map<String, Set<Long>>> maybeCellExtIdsToAppIdsMap = loadCellExtIdsToAppIdsMap(dsl, diagramId);

        if (OptionalUtilities.isEmpty(maybeCellExtIdsToAppIdsMap)) {
            return Collections.emptySet();
        } else {
            Map<String, Set<Long>> cellExtIdsToAppIdsMap = maybeCellExtIdsToAppIdsMap.get();
            Set<Long> diagramApplicationIds = calcExactAppIdsDiagram(
                    dsl,
                    cellExtIdsToAppIdsMap,
                    inScopeApplicationSelector);


        }

        return null;

    }


}
