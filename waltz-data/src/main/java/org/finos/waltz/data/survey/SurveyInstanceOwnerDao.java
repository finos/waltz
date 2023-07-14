/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.survey;

import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.ImmutableSurveyInstance;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceOwner;
import org.finos.waltz.model.survey.SurveyInstanceOwner;
import org.finos.waltz.model.survey.SurveyInstanceOwnerCreateCommand;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.schema.tables.records.SurveyInstanceOwnerRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE_OWNER;

@Repository
public class SurveyInstanceOwnerDao {
    private static final RecordMapper<Record, SurveyInstanceOwner> TO_DOMAIN_MAPPER = record ->
            ImmutableSurveyInstanceOwner.builder()
                    .id(record.getValue(SURVEY_INSTANCE_OWNER.ID))
                    .surveyInstance(ImmutableSurveyInstance.builder()
                            .id(record.getValue(SURVEY_INSTANCE.ID))
                            .surveyRunId(record.getValue(SURVEY_INSTANCE.SURVEY_RUN_ID))
                            .surveyEntity(EntityReference.mkRef(
                                    EntityKind.valueOf(record.getValue(SURVEY_INSTANCE.ENTITY_KIND)),
                                    record.getValue(SURVEY_INSTANCE.ENTITY_ID)))
                            .status(SurveyInstanceStatus.valueOf(record.getValue(SURVEY_INSTANCE.STATUS)))
                            .dueDate(record.getValue(SURVEY_INSTANCE.DUE_DATE).toLocalDate())
                            .approvalDueDate(record.getValue(SURVEY_INSTANCE.APPROVAL_DUE_DATE).toLocalDate())
                            .build())
                    .person(PersonDao.personMapper.map(record))
                    .build();

    private final DSLContext dsl;


    @Autowired
    public SurveyInstanceOwnerDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public boolean isPersonInstanceOwner(long personId, long surveyInstanceId) {
        Condition recipientExists = DSL
                .exists(DSL
                    .select(SURVEY_INSTANCE_OWNER.ID)
                    .from(SURVEY_INSTANCE_OWNER)
                    .where(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(surveyInstanceId)
                            .and(SURVEY_INSTANCE_OWNER.PERSON_ID.eq(personId))));

        return dsl
                .select(DSL
                        .when(recipientExists, true)
                        .otherwise(false))
                .fetchOne(Record1::value1);
    }


    public long create(SurveyInstanceOwnerCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyInstanceOwnerRecord record = dsl.newRecord(SURVEY_INSTANCE_OWNER);
        record.setSurveyInstanceId(command.surveyInstanceId());
        record.setPersonId(command.personId());

        return dsl
                .insertInto(SURVEY_INSTANCE_OWNER)
                .set(record)
                .onDuplicateKeyIgnore()
                .returning(SURVEY_INSTANCE_OWNER.ID)
                .execute();
    }


    public boolean delete(long surveyInstanceId,
                          long personId) {

        return dsl
                .deleteFrom(SURVEY_INSTANCE_OWNER)
                .where(SURVEY_INSTANCE_OWNER.PERSON_ID.eq(personId))
                .and(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .execute() == 1;
    }


    public int deleteForSurveyRun(long surveyRunId) {
        Select<Record1<Long>> surveyInstanceIdSelector = dsl.select(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId));

        return dsl.delete(SURVEY_INSTANCE_OWNER)
                .where(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.in(surveyInstanceIdSelector))
                .execute();
    }

    @Deprecated
    public List<SurveyInstanceOwner> findForSurveyInstance(long surveyInstanceId) {
        return dsl
                .select(SURVEY_INSTANCE_OWNER.fields())
                .select(SURVEY_INSTANCE.fields())
                .select(PERSON.fields())
                .from(SURVEY_INSTANCE_OWNER)
                .innerJoin(SURVEY_INSTANCE).on(SURVEY_INSTANCE.ID.eq(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID))
                .innerJoin(PERSON).on(PERSON.ID.eq(SURVEY_INSTANCE_OWNER.PERSON_ID))
                .where(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Person> findPeopleForSurveyInstance(long surveyInstanceId) {
        return dsl
                .select(PERSON.fields())
                .from(SURVEY_INSTANCE_OWNER)
                .innerJoin(SURVEY_INSTANCE).on(SURVEY_INSTANCE.ID.eq(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID))
                .innerJoin(PERSON).on(PERSON.ID.eq(SURVEY_INSTANCE_OWNER.PERSON_ID))
                .where(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(PersonDao.personMapper);
    }



    public Long getPersonIdForOwnerId(long ownerId) {
        return dsl
                .select(SURVEY_INSTANCE_OWNER.PERSON_ID)
                .from(SURVEY_INSTANCE_OWNER)
                .where(SURVEY_INSTANCE_OWNER.ID.eq(ownerId))
                .fetchOne(SURVEY_INSTANCE_OWNER.PERSON_ID);
    }
}
