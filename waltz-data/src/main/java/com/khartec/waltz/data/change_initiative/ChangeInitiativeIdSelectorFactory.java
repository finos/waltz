package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;

@Service
public class ChangeInitiativeIdSelectorFactory extends AbstractIdSelectorFactory {

    @Autowired
    public ChangeInitiativeIdSelectorFactory(DSLContext dsl) {
        super(dsl, EntityKind.CHANGE_INITIATIVE);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
                return mkForAppGroup(options);
            default:
                throw new UnsupportedOperationException("Cannot create Change Initiatives selector from kind: "+options.entityReference().kind());
        }

    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        switch (options.scope()) {
            case EXACT:
                return mkForAppGroupExact(options.entityReference().id());
            default:
                throw new UnsupportedOperationException("Cannot create Change Initiative selector from "
                        + options.entityReference().kind().name() + " with scope: " + options.scope());
        }
    }


    private Select<Record1<Long>> mkForAppGroupExact(long id) {
        return DSL.selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(id))
                    .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APP_GROUP.name()))
                    .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                    .and(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(RelationshipKind.RELATES_TO.name()));
    }

}
