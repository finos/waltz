package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.TO_DOMAIN_MAPPER;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM;

@Repository
public class AggregateOverlayDiagramDao {

    private final DSLContext dsl;

    @Autowired
    public AggregateOverlayDiagramDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public AggregateOverlayDiagram getById(Long diagramId) {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM)
                .where(AGGREGATE_OVERLAY_DIAGRAM.ID.eq(diagramId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<AggregateOverlayDiagram> findAll() {
        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM.fields())
                .from(AGGREGATE_OVERLAY_DIAGRAM)
                .fetchSet(TO_DOMAIN_MAPPER::map);
    }




}
