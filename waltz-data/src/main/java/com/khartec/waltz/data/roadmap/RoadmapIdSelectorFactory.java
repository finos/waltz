package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.impl.DSL.selectDistinct;

/**
 * Roadmaps are typically associated with entities in one of two ways:
 *
 * <ul>
 *      <li>directly (via the `entity_relationship` table)</li>
 *      <li>implicitly (via the scenario axis definitions and ratings)</li>
 * </ul>
 *
 *  Note, currently, this selector factory only supports the first of these options.
 */
@Service
public class RoadmapIdSelectorFactory implements IdSelectorFactory {


    @Autowired
    public RoadmapIdSelectorFactory() {
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {

        switch (options.entityReference().kind()) {
            case ORG_UNIT:
            case PERSON:
            case APP_GROUP:
                return mkViaRelationships(options);
            default:
                // update class comment if list of supported entities changes
                String msg = String.format(
                        "Cannot create Change Initiative Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkViaRelationships(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.ROADMAP.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));
    }

}
