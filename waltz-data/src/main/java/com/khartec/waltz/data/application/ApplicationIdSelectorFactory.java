package com.khartec.waltz.data.application;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.utils.IdUtilities;
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

            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: "+ref.kind());
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
                .selectDistinct(APPLICATION.ID)
                .from(APPLICATION)
                .where(dsl.renderInlined(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitIds)));
    }


    private SelectConditionStep<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        checkTrue(
                scope == EXACT,
                "Can only query for 'EXACT' app group matches, not: " + scope);
        return dsl
                .selectDistinct(APPLICATION_GROUP_ENTRY.APPLICATION_ID)
                .from(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(ref.id()));
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

        SelectConditionStep<Record1<String>> employeeIds = DSL.selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId));

        Condition condition = INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)
                .or(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));

        return dsl
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(dsl.renderInlined(condition));
    }


    private String findEmployeeId(EntityReference ref) {
        return dsl.select(PERSON.EMPLOYEE_ID)
                    .from(PERSON)
                    .where(PERSON.ID.eq(ref.id()))
                    .fetchOne(PERSON.EMPLOYEE_ID);
    }


    private Select<Record1<Long>> mkForSinglePerson(EntityReference ref) {

       String employeeId = dsl.select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(ref.id()))
                .fetchOne(PERSON.EMPLOYEE_ID);
        return dsl
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId));
    }


    private Select<Record1<Long>> mkForCapability(EntityReference ref, HierarchyQueryScope scope) {

        switch (scope) {
            case CHILDREN:
                return dsl
                        .selectDistinct(APP_CAPABILITY.APPLICATION_ID)
                        .from(APP_CAPABILITY)
                        .where(dsl.renderInlined(APP_CAPABILITY.CAPABILITY_ID.in(
                                dsl.select(CAPABILITY.ID)
                                        .from(CAPABILITY)
                                        .where(CAPABILITY.LEVEL_1.eq(ref.id()))
                                        .or(CAPABILITY.LEVEL_2.eq(ref.id()))
                                        .or(CAPABILITY.LEVEL_2.eq(ref.id()))
                                        .or(CAPABILITY.LEVEL_3.eq(ref.id()))
                                        .or(CAPABILITY.LEVEL_4.eq(ref.id()))
                                        .or(CAPABILITY.LEVEL_5.eq(ref.id())))));
            case PARENTS:
                Set<Long> ids = dsl.select(CAPABILITY.LEVEL_1, CAPABILITY.LEVEL_2, CAPABILITY.LEVEL_3, CAPABILITY.LEVEL_4, CAPABILITY.LEVEL_4)
                        .from(CAPABILITY)
                        .where(CAPABILITY.ID.eq(ref.id()))
                        .fetchOne(r -> r == null
                                ? Collections.emptySet()
                                : SetUtilities.fromArray(r.value1(), r.value2(), r.value3(), r.value4(), r.value5()));

                ids.remove(null);

                return dsl
                        .selectDistinct(APP_CAPABILITY.APPLICATION_ID)
                        .from(APP_CAPABILITY)
                        .where(dsl.renderInlined(APP_CAPABILITY.CAPABILITY_ID.in(ids)));
            case EXACT:
                return dsl
                        .selectDistinct(APP_CAPABILITY.APPLICATION_ID)
                        .from(APP_CAPABILITY)
                        .where(APP_CAPABILITY.CAPABILITY_ID.eq(ref.id()));

            default:
                throw new UnsupportedOperationException("Unexpected scope: "+scope);
        }


    }

}
