/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.data.software_catalog;

import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.software_catalog.ImmutableSoftwareVersion;
import com.khartec.waltz.model.software_catalog.SoftwareVersion;
import com.khartec.waltz.schema.tables.records.SoftwareVersionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static com.khartec.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;


@Repository
public class SoftwareVersionDao {

    private static final Logger LOG = LoggerFactory.getLogger(SoftwareVersionDao.class);


    private static final RecordMapper<Record, SoftwareVersion> TO_DOMAIN = r -> {
        SoftwareVersionRecord record = r.into(SOFTWARE_VERSION);
        return ImmutableSoftwareVersion.builder()
                .id(record.getId())
                .softwarePackageId(record.getSoftwarePackageId())
                .version(record.getVersion())
                .description(record.getDescription())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .releaseDate(record.getReleaseDate().toLocalDate())
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SoftwareVersionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<SoftwareVersion> findBySoftwarePackageId(long id) {
        return dsl.selectFrom(SOFTWARE_VERSION)
                .where(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(id))
                .fetch(TO_DOMAIN);
    }


    public List<SoftwareVersion> findBySoftwarePackageIds(Collection<Long> ids) {
        return dsl.selectFrom(SOFTWARE_VERSION)
                .where(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.in(ids))
                .fetch(TO_DOMAIN);
    }


    public List<SoftwareVersion> findByLicenceId(long id) {
        return dsl.select(SOFTWARE_VERSION.fields())
                .from(SOFTWARE_VERSION)
                .innerJoin(SOFTWARE_VERSION_LICENCE)
                .on(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION.ID))
                .where(SOFTWARE_VERSION_LICENCE.LICENCE_ID.eq(id))
                .fetch(TO_DOMAIN);
    }

}
