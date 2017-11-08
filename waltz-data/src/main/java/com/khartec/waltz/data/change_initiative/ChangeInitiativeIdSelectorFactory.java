package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static org.jooq.impl.DSL.selectDistinct;

@Service
public class ChangeInitiativeIdSelectorFactory extends AbstractIdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory;
    private final DSLContext dsl;


    @Autowired
    public ChangeInitiativeIdSelectorFactory(DSLContext dsl,
                                             OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory) {
        super(dsl, EntityKind.CHANGE_INITIATIVE);

        checkNotNull(organisationalUnitIdSelectorFactory, "organisationalUnitIdSelectorFactory cannot be null");

        this.dsl = dsl;
        this.organisationalUnitIdSelectorFactory = organisationalUnitIdSelectorFactory;
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case APPLICATION:
            case MEASURABLE:
                return mkForRef(options);
            case PERSON:
                return mkForPerson(options);
            case CHANGE_INITIATIVE:
                return mkForChangeInitiative(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            default:
                String msg = String.format(
                        "Cannot create Change Initiative Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return dsl.selectDistinct(FLOW_DIAGRAM_ENTITY.ENTITY_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        SelectConditionStep<Record1<String>> empIdSelector = DSL
                .selectDistinct(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(options.entityReference().id()));

        return dsl.selectDistinct(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(CHANGE_INITIATIVE.ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(INVOLVEMENT.EMPLOYEE_ID.in(empIdSelector));
    }


    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {
        Select<Record1<Long>> ouSelector = organisationalUnitIdSelectorFactory.apply(options);

        return dsl
                .selectDistinct(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID.in(ouSelector));
    }


    private Select<Record1<Long>> mkForChangeInitiative(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForRef(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        Select<Record1<Long>> aToB = selectDistinct(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()));

        Select<Record1<Long>> bToA = selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));

        return aToB.union(bToA);
    }

}
