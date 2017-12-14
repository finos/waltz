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

import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.schema.tables.records.PhysicalSpecDefnSampleFileRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefnSampleFile.PHYSICAL_SPEC_DEFN_SAMPLE_FILE;

@Repository
public class PhysicalSpecDefinitionSampleFileDao {

    public static final RecordMapper<? super Record, PhysicalSpecDefinitionSampleFile> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecDefnSampleFileRecord record = r.into(PHYSICAL_SPEC_DEFN_SAMPLE_FILE);
        return ImmutablePhysicalSpecDefinitionSampleFile.builder()
                .id(record.getId())
                .specDefinitionId(record.getSpecDefnId())
                .name(record.getName())
                .fileData(record.getFileData())
                .build();
    };


    private static final Function<PhysicalSpecDefinitionSampleFile, PhysicalSpecDefnSampleFileRecord> TO_RECORD_MAPPER = f -> {
        PhysicalSpecDefnSampleFileRecord record = new PhysicalSpecDefnSampleFileRecord();
        record.setSpecDefnId(f.specDefinitionId());
        record.setName(f.name());
        record.setFileData(f.fileData());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecDefinitionSampleFileDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public Optional<PhysicalSpecDefinitionSampleFile> findForSpecDefinition(long specDefinitionId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DEFN_SAMPLE_FILE)
                .where(PHYSICAL_SPEC_DEFN_SAMPLE_FILE.SPEC_DEFN_ID.eq(specDefinitionId))
                .fetchOptional(TO_DOMAIN_MAPPER);
    }


    public long create(PhysicalSpecDefinitionSampleFile sampleFile) {
        PhysicalSpecDefnSampleFileRecord record = TO_RECORD_MAPPER.apply(sampleFile);

        return dsl.insertInto(PHYSICAL_SPEC_DEFN_SAMPLE_FILE)
                .set(record)
                .returning(PHYSICAL_SPEC_DEFN_SAMPLE_FILE.ID)
                .fetchOne()
                .getId();
    }


    public int delete(long specDefinitionSampleFileId) {
        return dsl.deleteFrom(PHYSICAL_SPEC_DEFN_SAMPLE_FILE)
                .where(PHYSICAL_SPEC_DEFN_SAMPLE_FILE.ID.eq(specDefinitionSampleFileId))
                .execute();
    }


    public int deleteForSpecDefinition(long specDefinitionId) {
        return dsl.deleteFrom(PHYSICAL_SPEC_DEFN_SAMPLE_FILE)
                .where(PHYSICAL_SPEC_DEFN_SAMPLE_FILE.SPEC_DEFN_ID.eq(specDefinitionId))
                .execute();
    }
}
