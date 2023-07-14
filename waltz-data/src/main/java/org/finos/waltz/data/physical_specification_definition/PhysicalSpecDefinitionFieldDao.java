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

package org.finos.waltz.data.physical_specification_definition;

import org.finos.waltz.model.FieldDataType;
import org.finos.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionField;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import org.finos.waltz.schema.tables.records.PhysicalSpecDefnFieldRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;

@Repository
public class PhysicalSpecDefinitionFieldDao {

    public static final RecordMapper<? super Record, PhysicalSpecDefinitionField> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecDefnFieldRecord record = r.into(PHYSICAL_SPEC_DEFN_FIELD);
        return ImmutablePhysicalSpecDefinitionField.builder()
                .id(record.getId())
                .specDefinitionId(record.getSpecDefnId())
                .name(record.getName())
                .position(record.getPosition())
                .type(FieldDataType.valueOf(record.getType()))
                .description(record.getDescription())
                .logicalDataElementId(Optional.ofNullable(record.getLogicalDataElementId()))
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };


    private static final Function<PhysicalSpecDefinitionField, PhysicalSpecDefnFieldRecord> TO_RECORD_MAPPER = f -> {
        PhysicalSpecDefnFieldRecord record = new PhysicalSpecDefnFieldRecord();
        record.setSpecDefnId(f.specDefinitionId());
        record.setName(f.name());
        record.setPosition(f.position());
        record.setType(f.type().name());
        record.setLogicalDataElementId(f.logicalDataElementId().orElse(null));
        record.setDescription(f.description());
        record.setLastUpdatedAt(Timestamp.valueOf(f.lastUpdatedAt()));
        record.setLastUpdatedBy(f.lastUpdatedBy());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecDefinitionFieldDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<PhysicalSpecDefinitionField> findForSpecDefinition(long specDefinitionId) {
        return dsl
                .select(PHYSICAL_SPEC_DEFN_FIELD.fields())
                .from(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(specDefinitionId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecDefinitionField> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(PHYSICAL_SPEC_DEFN_FIELD.fields())
                .from(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(PhysicalSpecDefinitionField definitionField) {
        PhysicalSpecDefnFieldRecord record = TO_RECORD_MAPPER.apply(definitionField);

        return dsl.insertInto(PHYSICAL_SPEC_DEFN_FIELD)
                .set(record)
                .returning(PHYSICAL_SPEC_DEFN_FIELD.ID)
                .fetchOne()
                .getId();
    }


    public int delete(long specDefinitionFieldId) {
        return dsl.deleteFrom(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.ID.eq(specDefinitionFieldId))
                .execute();
    }


    public int deleteForSpecDefinition(long specDefinitionId) {
        return dsl.deleteFrom(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(specDefinitionId))
                .execute();
    }


    public int updateDescription(long fieldId, String description) {
        return dsl.update(PHYSICAL_SPEC_DEFN_FIELD)
                .set(PHYSICAL_SPEC_DEFN_FIELD.DESCRIPTION, description)
                .where(PHYSICAL_SPEC_DEFN_FIELD.ID.eq(fieldId))
                .execute();
    }


    public int updateLogicalDataElement(long fieldId, Long logicalDataElementId) {
        return dsl.update(PHYSICAL_SPEC_DEFN_FIELD)
                .set(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID, logicalDataElementId)
                .where(PHYSICAL_SPEC_DEFN_FIELD.ID.eq(fieldId))
                .execute();
    }
}
