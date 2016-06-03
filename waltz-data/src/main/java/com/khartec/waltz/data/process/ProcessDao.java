package com.khartec.waltz.data.process;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.process.ImmutableProcess;
import com.khartec.waltz.model.process.Process;
import com.khartec.waltz.schema.tables.records.ProcessRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.Process.PROCESS;

@Repository
public class ProcessDao {

    public static final RecordMapper<? super Record, Process> TO_DOMAIN = r -> {
        ProcessRecord record = r.into(PROCESS);
        return ImmutableProcess.builder()
                .id(record.getId())
                .parentId(Optional.ofNullable(record.getParentId()))
                .name(record.getName())
                .description(record.getDescription())
                .level(record.getLevel())
                .level1(record.getLevel_1())
                .level2(Optional.ofNullable(record.getLevel_2()))
                .level3(Optional.ofNullable(record.getLevel_3()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ProcessDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Process getById(long id) {
        return dsl.select(PROCESS.fields())
                .from(PROCESS)
                .where(PROCESS.ID.eq(id))
                .fetchOne(TO_DOMAIN);
    }

    public List<Process> findAll() {
        return dsl.select(PROCESS.fields())
                .from(PROCESS)
                .fetch(TO_DOMAIN);
    }

}
