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

package com.khartec.waltz.data.licence;


import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.ApprovalStatus;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.licence.ImmutableLicence;
import com.khartec.waltz.model.licence.Licence;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.LicenceRecord;
import org.jooq.*;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.Licence.LICENCE;
import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static com.khartec.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;


@Repository
public class LicenceDao {

    public static final RecordMapper<Record, Licence> TO_DOMAIN_MAPPER = r -> {
        LicenceRecord record = r.into(LicenceRecord.class);

        return ImmutableLicence.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .lastUpdated(UserTimestamp.mkForUser(record.getLastUpdatedBy(), record.getLastUpdatedAt()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;

    @Autowired
    public LicenceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<Licence> findAll() {
        return dsl.select(LICENCE.fields())
                .from(LICENCE)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Licence getById(long id) {
        LicenceRecord record = dsl.select(LICENCE.fields())
                .from(LICENCE)
                .where(LICENCE.ID.eq(id))
                .fetchOneInto(LicenceRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Licence record with id: " + id);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public Licence getByExternalId(String externalId) {
        LicenceRecord record = dsl.select(LICENCE.fields())
                .from(LICENCE)
                .where(LICENCE.EXTERNAL_ID.eq(externalId))
                .fetchOneInto(LicenceRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Licence record with external Id: " + externalId);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public List<Licence> findBySelector(Select<Record1<Long>> selector) {
        return dsl.select(LICENCE.fields())
                .from(LICENCE)
                .where(LICENCE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }



    public List<Tally<Long>> countApplications() {

        Field licenceId = DSL.field("licence_id", Long.class);

        Table<Record2<Long, Long>> appLicences = DSL.selectDistinct(SOFTWARE_USAGE.APPLICATION_ID, SOFTWARE_VERSION_LICENCE.LICENCE_ID)
                .from(SOFTWARE_VERSION_LICENCE)
                .innerJoin(SOFTWARE_USAGE)
                    .on(SOFTWARE_USAGE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID))
                .asTable("appLicences");

        return JooqUtilities.calculateLongTallies(
                dsl,
                appLicences,
                licenceId,
                DSL.trueCondition());
    }
}
