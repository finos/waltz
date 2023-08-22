package org.finos.waltz.data.aggregate_overlay_diagram;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.BackingEntityWidgetDatum;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableBackingEntityWidgetDatum;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class BackingEntityWidgetDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID,
                    AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                    newArrayList(
                            EntityKind.MEASURABLE,
                            EntityKind.APP_GROUP,
                            EntityKind.DATA_TYPE,
                            EntityKind.PERSON
                    ))
            .as("entity_name");

    private final DSLContext dsl;


    @Autowired
    public BackingEntityWidgetDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<BackingEntityWidgetDatum> findWidgetData(long diagramId) {

        Map<String, Set<EntityReference>> backingEntityData = dsl
                .select(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.CELL_EXTERNAL_ID,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_KIND,
                        AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.RELATED_ENTITY_ID)
                .select(ENTITY_NAME_FIELD)
                .from(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA)
                .where(AGGREGATE_OVERLAY_DIAGRAM_CELL_DATA.DIAGRAM_ID.eq(diagramId))
                .fetchSet(r -> tuple(r.get(0, String.class), r.get(1, String.class), r.get(2, Long.class), r.get(3, String.class)))
                .stream()
                .collect(groupingBy(
                        t -> t.v1,
                        mapping(t -> mkRef(EntityKind.valueOf(t.v2), t.v3, t.v4), toSet())));

        return backingEntityData
                .entrySet()
                .stream()
                .map(e -> {
                    String cellExtId = e.getKey();
                    Set<EntityReference> backingEntities = e.getValue();

                    return ImmutableBackingEntityWidgetDatum.builder()
                            .cellExternalId(cellExtId)
                            .backingEntityReferences(backingEntities)
                            .build();
                })
                .filter(d -> !d.backingEntityReferences().isEmpty())
                .collect(toSet());
    }

}
