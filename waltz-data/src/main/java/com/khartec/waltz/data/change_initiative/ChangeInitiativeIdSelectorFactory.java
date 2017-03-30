package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.impl.DSL.selectDistinct;

@Service
public class ChangeInitiativeIdSelectorFactory extends AbstractIdSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public ChangeInitiativeIdSelectorFactory(DSLContext dsl,
                                             ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        super(dsl, EntityKind.CHANGE_INITIATIVE);

        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");

        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
                return mkForAppGroup(options);
            case CHANGE_INITIATIVE:
                return mkForChangeInitiative(options);
            case ORG_UNIT:
            case MEASURABLE:
                return mkForApps(options);
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


    private Select<Record1<Long>> mkForChangeInitiative(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given a change initiative ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForApps(IdSelectionOptions options) {
        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(options);

        Select<Record1<Long>> aToB = selectDistinct(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.ID_B.in(appIds));

        Select<Record1<Long>> bToA = selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.ID_A.in(appIds));

        return aToB.union(bToA);
    }


    private Select<Record1<Long>> mkForAppGroupExact(long id) {
        return selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.ID_A.eq(id))
                    .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APP_GROUP.name()))
                    .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                    .and(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(RelationshipKind.RELATES_TO.name()));
    }

}
