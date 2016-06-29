/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.involvement;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.end_user_app.EndUserAppDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.involvement.ImmutableInvolvement;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.involvement.InvolvementKind;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


@Repository
public class InvolvementDao {

    private final DSLContext dsl;

    private final RecordMapper<Record, Involvement> involvementMapper = r -> {
        InvolvementRecord involvementRecord = r.into(InvolvementRecord.class);
        return ImmutableInvolvement.builder()
                .employeeId(involvementRecord.getEmployeeId())
                .kind(InvolvementKind.valueOf(involvementRecord.getKind()))
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(involvementRecord.getEntityKind()))
                        .id(involvementRecord.getEntityId())
                        .build())
                .provenance(involvementRecord.getProvenance())
                .build();
    };


    @Autowired
    public InvolvementDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        return dsl.select()
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(ref.kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.eq(ref.id()))
                .fetch(involvementMapper);
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        return dsl.select()
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .fetch(involvementMapper);
    }


    public List<Application> findDirectApplicationsByEmployeeId(String employeeId) {
        return dsl.select()
                .from(APPLICATION)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(APPLICATION.ID.eq(INVOLVEMENT.ENTITY_ID))
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }


    public List<Application> findAllApplicationsByEmployeeId(String employeeId) {
        SelectOrderByStep<Record1<String>> employeeIds = dsl
                .selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                .union(DSL.select(DSL.value(employeeId))
                        .from(PERSON_HIERARCHY));

        SelectConditionStep<Record1<Long>> applicationIds = dsl
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND
                        .eq(EntityKind.APPLICATION.name())
                        .and(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));

        SelectConditionStep<Record> query = dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .where(APPLICATION.ID.in(applicationIds));

        return query
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }


    public List<EndUserApplication> findAllEndUserApplicationsByEmployeeId(Select<Record1<Long>> endUserAppIdSelector) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.in(endUserAppIdSelector))
                .fetch(EndUserAppDao.END_USER_APP_MAPPER);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        return dsl.selectDistinct()
                .from(PERSON)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(ref.id()))
                .and(INVOLVEMENT.ENTITY_KIND.eq(ref.kind().name()))
                .where(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .fetch(PersonDao.personMapper);
    }


    public Collection<ChangeInitiative> findDirectChangeInitiativesByEmployeeId(String employeeId) {
        return dsl.selectDistinct()
                .from(CHANGE_INITIATIVE)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(CHANGE_INITIATIVE.ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .fetch(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
