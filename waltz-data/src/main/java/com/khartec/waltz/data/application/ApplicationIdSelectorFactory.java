package com.khartec.waltz.data.application;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.application.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;

@Service
public class ApplicationIdSelectorFactory implements Function<ApplicationIdSelectionOptions, Select<Record1<Long>>> {

    private final DSLContext dsl;
    private final OrganisationalUnitDao organisationalUnitDao;

    private final Application app = APPLICATION.as("app");
    private final AppCapability appCapability = APP_CAPABILITY.as("appcap");
    private final ApplicationGroupEntry appGroup = APPLICATION_GROUP_ENTRY.as("appgrp");
    private final Capability capability = CAPABILITY.as("cap");
    private final EntityRelationship relationship = EntityRelationship.ENTITY_RELATIONSHIP.as("relationship");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");



    @Autowired
    public ApplicationIdSelectorFactory(DSLContext dsl, OrganisationalUnitDao organisationalUnitDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");

        this.dsl = dsl;
        this.organisationalUnitDao = organisationalUnitDao;
    }


    @Override
    public Select<Record1<Long>> apply(ApplicationIdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case APP_GROUP:
                return mkForAppGroup(ref, options.scope());
            case PERSON:
                return mkForPerson(ref, options.scope());
            case CAPABILITY:
                return mkForCapability(ref, options.scope());
            case ORG_UNIT:
                return mkForOrgUnit(ref, options.scope());
            case PROCESS:
                return mkForProcess(ref, options.scope());

            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: "+ref.kind());
        }
    }

    private Select<Record1<Long>> mkForProcess(EntityReference ref, HierarchyQueryScope scope) {
        switch (scope) {
            case EXACT:
                return dsl.select(relationship.ID_A)
                        .from(relationship)
                        .where(relationship.KIND_A.eq(EntityKind.APPLICATION.name()))
                        .and(relationship.RELATIONSHIP.eq(RelationshipKind.PARTICIPATES_IN.name()))
                        .and(relationship.KIND_B.eq(EntityKind.PROCESS.name()))
                        .and(relationship.ID_B.eq(ref.id()));

            default:
                throw new UnsupportedOperationException("Querying for appIds related to processes using (scope: '"
                        + scope
                        + "') not supported");
        }
    }


    private SelectConditionStep<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {
        Set<Long> orgUnitIds = new HashSet<>();
        orgUnitIds.add(ref.id());

        switch (scope) {
            case EXACT:
                break;
            case CHILDREN:
                List<OrganisationalUnit> subUnits = organisationalUnitDao.findDescendants(ref.id());
                orgUnitIds.addAll(IdUtilities.toIds(subUnits));
                break;
            case PARENTS:
                List<OrganisationalUnit> parentUnits = organisationalUnitDao.findAncestors(ref.id());
                orgUnitIds.addAll(IdUtilities.toIds(parentUnits));
                break;
        }

        return dsl
                .selectDistinct(app.ID)
                .from(app)
                .where(dsl.renderInlined(app.ORGANISATIONAL_UNIT_ID.in(orgUnitIds)));
    }


    private SelectConditionStep<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        checkTrue(
                scope == EXACT,
                "Can only query for 'EXACT' app group matches, not: " + scope);
        return dsl
                .selectDistinct(appGroup.APPLICATION_ID)
                .from(appGroup)
                .where(appGroup.GROUP_ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkForPerson(EntityReference ref, HierarchyQueryScope scope) {
        switch (scope) {
            case EXACT:
                return mkForSinglePerson(ref);
            case CHILDREN:
                return mkForPersonReportees(ref);
           default:
                throw new UnsupportedOperationException(
                        "Querying for appIds of person using (scope: '"
                                + scope
                                + "') not supported");
        }
    }

    private Select<Record1<Long>> mkForPersonReportees(EntityReference ref) {
        String employeeId = findEmployeeId(ref);

        SelectConditionStep<Record1<String>> employeeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(employeeId));

        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(involvement.EMPLOYEE_ID.eq(employeeId)
                .or(involvement.EMPLOYEE_ID.in(employeeIds)));

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


    private Select<Record1<Long>> mkForSinglePerson(EntityReference ref) {

       String employeeId = dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()))
                .fetchOne(person.EMPLOYEE_ID);
        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .where(involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId));
    }


    private Select<Record1<Long>> mkForCapability(EntityReference ref, HierarchyQueryScope scope) {

        switch (scope) {
            case CHILDREN:
                return dsl
                        .selectDistinct(appCapability.APPLICATION_ID)
                        .from(appCapability)
                        .where(dsl.renderInlined(appCapability.CAPABILITY_ID.in(
                                dsl.select(capability.ID)
                                        .from(capability)
                                        .where(capability.LEVEL_1.eq(ref.id()))
                                        .or(capability.LEVEL_2.eq(ref.id()))
                                        .or(capability.LEVEL_2.eq(ref.id()))
                                        .or(capability.LEVEL_3.eq(ref.id()))
                                        .or(capability.LEVEL_4.eq(ref.id()))
                                        .or(capability.LEVEL_5.eq(ref.id())))));
            case PARENTS:
                Set<Long> ids = dsl.select(capability.LEVEL_1, capability.LEVEL_2, capability.LEVEL_3, capability.LEVEL_4, capability.LEVEL_4)
                        .from(capability)
                        .where(capability.ID.eq(ref.id()))
                        .fetchOne(r -> r == null
                                ? Collections.emptySet()
                                : SetUtilities.fromArray(r.value1(), r.value2(), r.value3(), r.value4(), r.value5()));

                ids.remove(null);

                return dsl
                        .selectDistinct(appCapability.APPLICATION_ID)
                        .from(appCapability)
                        .where(dsl.renderInlined(appCapability.CAPABILITY_ID.in(ids)));
            case EXACT:
                return dsl
                        .selectDistinct(appCapability.APPLICATION_ID)
                        .from(appCapability)
                        .where(appCapability.CAPABILITY_ID.eq(ref.id()));

            default:
                throw new UnsupportedOperationException("Unexpected scope: "+scope);
        }


    }

}
