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

package org.finos.waltz.data.licence;


import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.licence.ImmutableLicence;
import org.finos.waltz.model.licence.Licence;
import org.finos.waltz.model.licence.SaveLicenceCommand;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.tables.records.LicenceRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.tables.Licence.LICENCE;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;


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

        Field<Long> licenceId = DSL.field("licence_id", Long.class);

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

    public boolean save(SaveLicenceCommand cmd, String username) {

        LicenceRecord licenceRecord = dsl.newRecord(LICENCE);

        licenceRecord.setName(cmd.name());
        licenceRecord.setDescription(cmd.description());
        licenceRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        licenceRecord.setLastUpdatedBy(username);
        licenceRecord.setProvenance("waltz");

        if (cmd.id().isPresent()) {
            licenceRecord.setId(cmd.id().get());
            licenceRecord.changed(LICENCE.ID, false);
        } else {
            licenceRecord.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
            licenceRecord.setCreatedBy(username);
        }

        cmd.externalId().ifPresent(licenceRecord::setExternalId);

        int stored = licenceRecord.store();

        return stored == 1;
    }

    public boolean remove(long licenceId) {
        return dsl
                .deleteFrom(LICENCE)
                .where(LICENCE.ID.eq(licenceId))
                .execute() == 1;
    }
}
