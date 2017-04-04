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


    public List<PhysicalSpecDefinitionSampleFile> findForSpecDefinition(long specDefinitionId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DEFN_SAMPLE_FILE)
                .where(PHYSICAL_SPEC_DEFN_SAMPLE_FILE.SPEC_DEFN_ID.eq(specDefinitionId))
                .fetch(TO_DOMAIN_MAPPER);
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
