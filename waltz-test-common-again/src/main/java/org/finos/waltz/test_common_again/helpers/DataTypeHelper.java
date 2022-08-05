package org.finos.waltz.test_common_again.helpers;

import org.finos.waltz.schema.tables.records.DataTypeRecord;
import org.finos.waltz.service.data_type.DataTypeService;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static org.finos.waltz.schema.Tables.DATA_TYPE;

@Service
public class DataTypeHelper {

    private static final AtomicLong counter = new AtomicLong(100);

    @Autowired
    private DSLContext dsl;

    @Autowired
    private DataTypeService svc;


    public Long createDataType(String name) {
        DataTypeRecord record = dsl.newRecord(DATA_TYPE);

        record.setId(counter.incrementAndGet());
        String uniqName = NameHelper.mkName(name);

        record.setName(uniqName);
        record.setCode(uniqName);
        record.setDescription(uniqName);

        record.insert();

        return record.getId();
    }


    public long createUnknownDatatype() {
        clearAllDataTypes();

        long id = 1L;

        dsl.insertInto(DATA_TYPE)
                .columns(
                        DATA_TYPE.ID,
                        DATA_TYPE.NAME,
                        DATA_TYPE.DESCRIPTION,
                        DATA_TYPE.CODE,
                        DATA_TYPE.CONCRETE,
                        DATA_TYPE.UNKNOWN.as(DSL.quotedName("unknown"))) //TODO: as part of #5639 can drop quotedName
                .values(id, "Unknown", "Unknown data type", "UNKNOWN", false, true)
                .execute();

        return id;
    }


    public void createDataType(Long id, String name, String code) {

        dsl.insertInto(DATA_TYPE)
                .columns(
                        DATA_TYPE.ID,
                        DATA_TYPE.NAME,
                        DATA_TYPE.DESCRIPTION,
                        DATA_TYPE.CODE)
                .values(id, name, name, code)
                .execute();
    }


    public void clearAllDataTypes() {
        dsl.deleteFrom(DATA_TYPE).execute();
    }


}
