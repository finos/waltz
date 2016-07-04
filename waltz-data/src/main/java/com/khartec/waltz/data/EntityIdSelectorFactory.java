package com.khartec.waltz.data;

import com.khartec.waltz.model.EntityIdSelectionOptions;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.EntityRelationship;
import com.khartec.waltz.schema.tables.Involvement;
import com.khartec.waltz.schema.tables.Person;
import com.khartec.waltz.schema.tables.PersonHierarchy;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;

@Service
public abstract class EntityIdSelectorFactory implements Function<EntityIdSelectionOptions, Select<Record1<Long>>> {

    protected final DSLContext dsl;

    private final EntityRelationship relationship = EntityRelationship.ENTITY_RELATIONSHIP.as("relationship");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");



    @Autowired
    public EntityIdSelectorFactory(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    @Override
    public final Select<Record1<Long>> apply(EntityIdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityKind desiredKind = options.desiredKind();
        EntityReference ref = options.entityReference();

        switch (ref.kind()) {
            case APP_GROUP:
                return mkForAppGroup(ref, options.scope());
            case CAPABILITY:
                return mkForCapability(ref, options.scope());
            case PERSON:
                return mkForPerson(desiredKind, ref, options.scope());
            case PROCESS:
                return mkForProcess(desiredKind, ref, options.scope());
            case ORG_UNIT:
                return mkForOrgUnit(ref, options.scope());

            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: "+ref.kind());
        }
    }


    protected abstract Select<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope);


    protected abstract Select<Record1<Long>> mkForCapability(EntityReference ref, HierarchyQueryScope scope);


    protected abstract Select<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope);


    private Select<Record1<Long>> mkForPerson(EntityKind desiredKind, EntityReference ref, HierarchyQueryScope scope) {
        switch (scope) {
            case EXACT:
                return mkForSinglePerson(desiredKind, ref);
            case CHILDREN:
                return mkForPersonReportees(desiredKind, ref);
            default:
                throw new UnsupportedOperationException(
                        "Querying for appIds of person using (scope: '"
                                + scope
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForPersonReportees(EntityKind desiredKind, EntityReference ref) {
        String employeeId = findEmployeeId(ref);

        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(employeeId));

        Condition condition = involvement.ENTITY_KIND.eq(desiredKind.name())
                .and(involvement.EMPLOYEE_ID.eq(employeeId)
                        .or(involvement.EMPLOYEE_ID.in(reporteeIds)));

        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .where(dsl.renderInlined(condition));
    }


    private String findEmployeeId(EntityReference ref) {
        return dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()))
                .fetchOne(person.EMPLOYEE_ID);
    }


    private Select<Record1<Long>> mkForSinglePerson(EntityKind desiredKind, EntityReference ref) {

        String employeeId = dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()))
                .fetchOne(person.EMPLOYEE_ID);
        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .where(involvement.ENTITY_KIND.eq(desiredKind.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId));
    }


    private Select<Record1<Long>> mkForProcess(EntityKind desiredKind, EntityReference ref, HierarchyQueryScope scope) {
        switch (scope) {
            case EXACT:
                return dsl.select(relationship.ID_A)
                        .from(relationship)
                        .where(relationship.KIND_A.eq(desiredKind.name()))
                        .and(relationship.RELATIONSHIP.eq(RelationshipKind.PARTICIPATES_IN.name()))
                        .and(relationship.KIND_B.eq(EntityKind.PROCESS.name()))
                        .and(relationship.ID_B.eq(ref.id()));

            default:
                throw new UnsupportedOperationException("Querying for appIds related to processes using (scope: '"
                        + scope
                        + "') not supported");
        }
    }

}
