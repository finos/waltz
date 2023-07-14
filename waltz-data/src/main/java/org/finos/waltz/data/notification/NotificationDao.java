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

package org.finos.waltz.data.notification;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.notification.ImmutableNotificationSummary;
import org.finos.waltz.model.notification.NotificationSummary;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.schema.Tables.*;


@Repository
public class NotificationDao {

    private static final Field<Integer> COUNT = DSL.count().as("count");

    private final DSLContext dsl;


    private static final RecordMapper<Record, NotificationSummary> TO_DOMAIN_MAPPER = r -> {
        Integer c = r.get("count", Integer.class);
        return ImmutableNotificationSummary
                .builder()
                .kind(EntityKind.valueOf(r.get("kind", String.class)))
                .count(c)
                .build();
    };


    @Autowired
    public NotificationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<NotificationSummary> findNotificationsByUserId(String userId) {
        Select<Record2<String, Integer>> attestationCount = DSL
                .select(DSL.val(EntityKind.ATTESTATION.name()).as("kind"),
                        COUNT)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());

        Select<Record2<String, Integer>> surveyCount = DSL
                .select(DSL.val(EntityKind.SURVEY_INSTANCE.name()).as("kind"),
                        COUNT)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .innerJoin(PERSON)
                .on(PERSON.ID.eq(SURVEY_INSTANCE_RECIPIENT.PERSON_ID))
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .where(PERSON.EMAIL.eq(userId))
                .and(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull())
                .and(SURVEY_INSTANCE.STATUS.in(asList(
                        SurveyInstanceStatus.NOT_STARTED.name(),
                        SurveyInstanceStatus.IN_PROGRESS.name())))
                .and(SURVEY_TEMPLATE.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()));


        Select<Record2<String, Integer>> qry = attestationCount
                .unionAll(surveyCount);

        return dsl
                .resultQuery(dsl.renderInlined(qry))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
