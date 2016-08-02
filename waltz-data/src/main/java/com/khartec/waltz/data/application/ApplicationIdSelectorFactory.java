package com.khartec.waltz.data.application;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.capability.CapabilityIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static com.khartec.waltz.schema.tables.Process.PROCESS;

@Service
public class ApplicationIdSelectorFactory implements IdSelectorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationIdSelectorFactory.class);

    private final DSLContext dsl;
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;
    private final CapabilityIdSelectorFactory capabilityIdSelectorFactory;

    private final Application app = APPLICATION.as("app");
    private final AppCapability appCapability = APP_CAPABILITY.as("appcap");
    private final ApplicationGroupEntry appGroup = APPLICATION_GROUP_ENTRY.as("appgrp");
    private final EntityRelationship relationship = EntityRelationship.ENTITY_RELATIONSHIP.as("relationship");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");


    @Autowired
    public ApplicationIdSelectorFactory(DSLContext dsl,
                                        CapabilityIdSelectorFactory capabilityIdSelectorFactory,
                                        OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(capabilityIdSelectorFactory, "capabilityIdSelectorFactory cannot be null");
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");

        this.dsl = dsl;
        this.capabilityIdSelectorFactory = capabilityIdSelectorFactory;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
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
                SelectSelectStep<Record1<Long>> exactProcessIdSelector = dsl.select(DSL.val(ref.id()));
                return mkForProcess(exactProcessIdSelector);
            case CHILDREN:
                SelectConditionStep<Record1<Long>> childProcessIdSelector = dsl.select(PROCESS.ID)
                        .from(PROCESS)
                        .where(PROCESS.LEVEL_1.eq(ref.id())
                                .or(PROCESS.LEVEL_2.eq(ref.id()))
                                .or(PROCESS.LEVEL_3.eq(ref.id())));
                return mkForProcess(childProcessIdSelector);

            default:
                throw new UnsupportedOperationException("Querying for appIds related to processes using (scope: '"
                        + scope
                        + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForProcess(Select<Record1<Long>> processIdSelector) {
        return dsl.select(relationship.ID_A)
                .from(relationship)
                .where(relationship.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(relationship.RELATIONSHIP.eq(RelationshipKind.PARTICIPATES_IN.name()))
                .and(relationship.KIND_B.eq(EntityKind.PROCESS.name()))
                .and(relationship.ID_B.in(processIdSelector));
    }


    private SelectConditionStep<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {

        ImmutableIdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        return dsl
                .selectDistinct(app.ID)
                .from(app)
                .where(dsl.renderInlined(app.ORGANISATIONAL_UNIT_ID.in(ouSelector)));
    }


    private SelectConditionStep<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        if (scope != EXACT) {
            LOG.info("App Groups are not hierarchical therefore ignoring requested scope of: "+scope);
        }
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

        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(employeeId));

        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
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

        ImmutableIdSelectionOptions capabilitySelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> capabilitySelector = capabilityIdSelectorFactory.apply(capabilitySelectorOptions);

        return dsl
                .selectDistinct(appCapability.APPLICATION_ID)
                .from(appCapability)
                .where(dsl.renderInlined(appCapability.CAPABILITY_ID.in(capabilitySelector)));
    }

}
