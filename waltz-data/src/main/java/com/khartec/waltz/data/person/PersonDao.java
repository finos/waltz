/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.person;

import com.khartec.waltz.model.person.ImmutablePerson;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.person.PersonKind;
import com.khartec.waltz.schema.tables.records.PersonRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


@Repository
public class PersonDao {

    private static final Logger LOG = LoggerFactory.getLogger(PersonDao.class);
    public static final RecordMapper<? super Record, Person> personMapper = r -> {
        PersonRecord record = r.into(PersonRecord.class);
        return ImmutablePerson.builder()
                .id(record.getId())
                .employeeId(record.getEmployeeId())
                .displayName(record.getDisplayName())
                .email(record.getEmail())
                .personKind(PersonKind.valueOf(record.getKind()))
                .userPrincipalName(Optional.ofNullable(record.getUserPrincipalName()))
                .departmentName(Optional.ofNullable(record.getDepartmentName()))
                .managerEmployeeId(Optional.ofNullable(record.getManagerEmployeeId()))
                .title(Optional.ofNullable(record.getTitle()))
                .mobilePhone(Optional.ofNullable(record.getMobilePhone()))
                .officePhone(Optional.ofNullable(record.getOfficePhone()))
                .organisationalUnitId(Optional.ofNullable(record.getOrganisationalUnitId()))
                .isRemoved(record.getIsRemoved())
                .build();
    };
    private final DSLContext dsl;


    @Autowired
    public PersonDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public Person getByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find person without an employeeId");
        return dsl.select()
                .from(PERSON)
                .where(PERSON.EMPLOYEE_ID.eq(employeeId))
                .fetchOne(personMapper);
    }


    public Person getById(long id) {
        return dsl.select()
                .from(PERSON)
                .where(PERSON.ID.eq(id))
                .fetchOne(personMapper);
    }


    public Person getByUserName(String userName) {
        checkNotEmpty(userName, "Cannot find person without a userName");
        return dsl.select()
                .from(PERSON)
                .where(PERSON.EMAIL.eq(userName))
                .andNot(PERSON.IS_REMOVED)
                .fetchOne(personMapper);
    }


    public List<Person> findDirectsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return dsl.select()
                .from(PERSON)
                .where(dsl.renderInlined(PERSON.MANAGER_EMPLOYEE_ID.eq(employeeId)))
                .andNot(PERSON.IS_REMOVED)
                .orderBy(PERSON.DISPLAY_NAME)
                .fetch(personMapper);
    }


    /**
     * Returned in order, immediate manager first
     **/
    public List<Person> findAllManagersByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return dsl.select(PERSON.fields())
                .from(PERSON)
                .join(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.MANAGER_ID))
                .where(PERSON_HIERARCHY.EMPLOYEE_ID.eq(employeeId))
                .andNot(PERSON.IS_REMOVED)
                .orderBy(PERSON_HIERARCHY.LEVEL.desc())
                .fetch(personMapper);
    }


    public List<Person> all() {
        return dsl.select()
                .from(PERSON)
                .where(PERSON.IS_REMOVED.eq(false))
                .fetch(personMapper);
    }


    public int[] bulkSave(List<ImmutablePerson> people) {

        checkNotNull(people, "Cannot bulk save a null collection of people");

        LOG.info("Bulk saving " + people.size() + "records");

        List<PersonRecord> records = people.stream()
                .map(p -> {
                    PersonRecord r = dsl.newRecord(PERSON);
                    r.setDisplayName(p.displayName());
                    r.setEmployeeId(p.employeeId());
                    r.setEmail(p.email());
                    r.setIsRemoved(p.isRemoved());
                    r.setKind(p.personKind().name());
                    r.setDepartmentName(p.departmentName().orElse(""));
                    r.setManagerEmployeeId(p.managerEmployeeId().orElse(""));
                    r.setTitle(p.title().orElse(""));
                    r.setOfficePhone(p.officePhone().orElse(""));
                    r.setMobilePhone(p.mobilePhone().orElse(""));
                    r.setUserPrincipalName(p.userPrincipalName().orElse(""));
                    r.setOrganisationalUnitId(p.organisationalUnitId().orElse(null));
                    return r;
                })
                .collect(Collectors.toList());

        return dsl.batchInsert(records).execute();
    }


    public Person getPersonByUserId(String userId) {
        return dsl.select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.EMAIL.eq(userId)) // TODO: change as part of 247
                .fetchOne(personMapper);
    }


    public List<Person> findPersonsByAttestationInstanceId(long instanceId) {
        return dsl.select(PERSON.fields())
                .from(ATTESTATION_INSTANCE_RECIPIENT)
                .innerJoin(PERSON)
                .on(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(PERSON.EMAIL))
                .where(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(instanceId))
                .andNot(PERSON.IS_REMOVED)
                .fetch(personMapper);
    }


    public Map<PersonKind, Integer> countAllUnderlingsByKind(String employeeId){

        Field<Integer> countField = DSL.count().as("count");

        return dsl
                .select(PERSON.KIND, countField)
                .from(PERSON_HIERARCHY)
                .innerJoin(PERSON)
                .on(PERSON_HIERARCHY.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                .andNot(PERSON.IS_REMOVED)
                .groupBy(PERSON.KIND)
                .fetchMap(r -> PersonKind.valueOf(r.get(PERSON.KIND)), r -> r.get(countField));
    }

}
