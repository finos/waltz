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

package com.khartec.waltz.data.physical_specification_definition;

import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinition;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionType;
import com.khartec.waltz.schema.tables.records.PhysicalSpecDefnRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefn.PHYSICAL_SPEC_DEFN;

@Repository
public class PhysicalSpecDefinitionDao {

    public static final RecordMapper<? super Record, PhysicalSpecDefinition> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecDefnRecord record = r.into(PHYSICAL_SPEC_DEFN);
        return ImmutablePhysicalSpecDefinition.builder()
                .id(record.getId())
                .specificationId(record.getSpecificationId())
                .version(record.getVersion())
                .status(ReleaseLifecycleStatus.valueOf(record.getStatus()))
                .delimiter(Optional.ofNullable(record.getDelimiter()))
                .type(PhysicalSpecDefinitionType.valueOf(record.getType()))
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .createdBy(record.getCreatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private static final Function<PhysicalSpecDefinition, PhysicalSpecDefnRecord> TO_RECORD_MAPPER = def -> {
        PhysicalSpecDefnRecord record = new PhysicalSpecDefnRecord();
        record.setSpecificationId(def.specificationId());
        record.setVersion(def.version());
        record.setStatus(def.status().name());
        record.setDelimiter(def.delimiter().orElse(null));
        record.setType(def.type().name());
        record.setProvenance(def.provenance());
        record.setCreatedAt(Timestamp.valueOf(def.createdAt()));
        record.setCreatedBy(def.createdBy());
        record.setLastUpdatedAt(Timestamp.valueOf(def.lastUpdatedAt()));
        record.setLastUpdatedBy(def.lastUpdatedBy());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public PhysicalSpecDefinition getById(long specDefinitionId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DEFN)
                .where(PHYSICAL_SPEC_DEFN.ID.eq(specDefinitionId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecDefinition> findForSpecification(long specificationId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DEFN)
                .where(PHYSICAL_SPEC_DEFN.SPECIFICATION_ID.eq(specificationId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecDefinition> findBySelector(Select<Record1<Long>> selector) {
        return dsl.selectFrom(PHYSICAL_SPEC_DEFN)
                .where(PHYSICAL_SPEC_DEFN.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(PhysicalSpecDefinition specDefinition) {
        PhysicalSpecDefnRecord record = TO_RECORD_MAPPER.apply(specDefinition);

        return dsl.insertInto(PHYSICAL_SPEC_DEFN)
                .set(record)
                .returning(PHYSICAL_SPEC_DEFN.ID)
                .fetchOne()
                .getId();
    }


    public int delete(long specDefinitionId) {
        return dsl.deleteFrom(PHYSICAL_SPEC_DEFN)
                .where(PHYSICAL_SPEC_DEFN.ID.eq(specDefinitionId))
                .execute();
    }


    public int updateStatus(long specDefinitionId, ReleaseLifecycleStatus newStatus, String userName) {
        return dsl.update(PHYSICAL_SPEC_DEFN)
                .set(PHYSICAL_SPEC_DEFN.STATUS, newStatus.name())
                .set(PHYSICAL_SPEC_DEFN.LAST_UPDATED_AT, nowUtcTimestamp())
                .set(PHYSICAL_SPEC_DEFN.LAST_UPDATED_BY, userName)
                .where(PHYSICAL_SPEC_DEFN.ID.eq(specDefinitionId))
                .and(PHYSICAL_SPEC_DEFN.STATUS.ne(newStatus.name()))
                .execute();
    }


    public int markExistingActiveAsDeprecated(long specificationId, String userName) {
        return dsl.update(PHYSICAL_SPEC_DEFN)
                .set(PHYSICAL_SPEC_DEFN.STATUS, ReleaseLifecycleStatus.DEPRECATED.name())
                .set(PHYSICAL_SPEC_DEFN.LAST_UPDATED_AT, nowUtcTimestamp())
                .set(PHYSICAL_SPEC_DEFN.LAST_UPDATED_BY, userName)
                .where(PHYSICAL_SPEC_DEFN.SPECIFICATION_ID.eq(specificationId))
                .and(PHYSICAL_SPEC_DEFN.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()))
                .execute();
    }
}
