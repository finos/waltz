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

package org.finos.waltz.data.end_user_app;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.enduserapp.EndUserApplication;
import org.finos.waltz.model.enduserapp.ImmutableEndUserApplication;
import org.finos.waltz.model.physical_flow.CriticalityValue;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.tables.records.EndUserApplicationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

@Repository
public class EndUserAppDao {

    private final DSLContext dsl;
    private static final Condition COMMON_CONDITION = END_USER_APPLICATION.IS_PROMOTED.isFalse()
            .and(END_USER_APPLICATION.LIFECYCLE_PHASE.in(
                    LifecyclePhase.PRODUCTION.name(),
                    LifecyclePhase.DEVELOPMENT.name()));

    public static final RecordMapper<Record, EndUserApplication> TO_DOMAIN_MAPPER = r -> {
        EndUserApplicationRecord record = r.into(END_USER_APPLICATION);
        return ImmutableEndUserApplication.builder()
                .name(record.getName())
                .description(mkSafe(record.getDescription()))
                .externalId(ofNullable(record.getExternalId()))
                .applicationKind(record.getKind())
                .id(record.getId())
                .organisationalUnitId(record.getOrganisationalUnitId())
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .riskRating(CriticalityValue.of(record.getRiskRating()))
                .provenance(record.getProvenance())
                .isPromoted(record.getIsPromoted())
                .build();
    };

    @Autowired
    public EndUserAppDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tally<Long>> countByOrganisationalUnit() {
        return JooqUtilities.calculateLongTallies(
                dsl,
                END_USER_APPLICATION,
                END_USER_APPLICATION.ORGANISATIONAL_UNIT_ID,
                COMMON_CONDITION
                );
    }

    @Deprecated
    public List<EndUserApplication> findByOrganisationalUnitSelector(Select<Record1<Long>> selector) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ORGANISATIONAL_UNIT_ID.in(selector)
                        .and(COMMON_CONDITION))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<EndUserApplication> findBySelector(Select<Record1<Long>> selector) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.in(selector)
                        .and(COMMON_CONDITION))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int updateIsPromotedFlag(long id) {
        return dsl.update(END_USER_APPLICATION)
                .set(END_USER_APPLICATION.IS_PROMOTED, true)
                .where(END_USER_APPLICATION.ID.eq(id))
                .execute();
    }


    public EndUserApplication getById(Long id) {
        return dsl
                .select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<EndUserApplication> findAll() {
        return dsl
                .select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(COMMON_CONDITION)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
