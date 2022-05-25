package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.BackingEntity;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableBackingEntity;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.aggregate_overlay_diagram.AggregateOverlayDiagramUtilities.TO_DOMAIN_MAPPER;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;

@Repository
public class AggregateOverlayDiagramDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID,
                    AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                    newArrayList(
                            EntityKind.MEASURABLE,
                            EntityKind.APP_GROUP,
                            EntityKind.PERSON
                    ))
            .as("entity_name");

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


    public Set<BackingEntity> findBackingEntities(Long diagramId) {

        return dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .select(ENTITY_NAME_FIELD)
                .from(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId))
                .fetchSet(r -> {
                    String cellExtId = r.get(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID);

                    EntityReference backingEntityRef = JooqUtilities.readRef(
                            r,
                            AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                            AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID,
                            ENTITY_NAME_FIELD);

                    return ImmutableBackingEntity.builder()
                            .cellId(cellExtId)
                            .entityReference(backingEntityRef)
                            .build();
                });
        }
}
