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

package com.khartec.waltz.data.notification;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.notification.ImmutableNotificationSummary;
import com.khartec.waltz.model.notification.NotificationSummary;
import com.khartec.waltz.model.survey.SurveyInstanceStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;


@Repository
public class NotificationDao {

    private final DSLContext dsl;

    private static final Field<Integer> COUNT = DSL.count().as("count");

    private static final RecordMapper<Record, NotificationSummary> TO_DOMAIN_MAPPER = r -> {
        return ImmutableNotificationSummary.builder()
                .kind(EntityKind.valueOf(r.get("kind", String.class)))
                .count(r.get(COUNT))
                .build();
    };


    @Autowired
    public NotificationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<NotificationSummary> findNotificationsByUserId(String userId) {
        Select<Record2<String, Integer>> attestationCount = dsl
                .select(DSL.val(EntityKind.ATTESTATION.name()).as("kind"), COUNT)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());

        Select<Record2<String, Integer>> surveyCount = dsl
                .select(DSL.val(EntityKind.SURVEY_INSTANCE.name()).as("kind"), COUNT)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .innerJoin(PERSON)
                .on(PERSON.ID.eq(SURVEY_INSTANCE_RECIPIENT.PERSON_ID))
                .where(PERSON.EMAIL.eq(userId))
                .and(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull())
                .and(SURVEY_INSTANCE.STATUS.eq(SurveyInstanceStatus.NOT_STARTED.name())
                        .or(SURVEY_INSTANCE.STATUS.eq(SurveyInstanceStatus.IN_PROGRESS.name())));

        return attestationCount
                .union(surveyCount)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
