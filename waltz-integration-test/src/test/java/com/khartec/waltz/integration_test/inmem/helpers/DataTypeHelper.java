package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.schema.tables.records.DataTypeRecord;
import org.jooq.DSLContext;

import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static com.khartec.waltz.schema.Tables.DATA_TYPE;

public class DataTypeHelper {

    private static final AtomicLong ctr = new AtomicLong();

    private final DSLContext dsl;

    private final LogicalFlowDao logicalFlowDao;

    public DataTypeHelper(LogicalFlowDao logicalFlowDao, DSLContext dsl) {

        this.logicalFlowDao = logicalFlowDao;
        this.dsl = dsl;
    }


    public Long createDataType(String name) {
        DataTypeRecord record = dsl.newRecord(DATA_TYPE);

        record.setId(ctr.incrementAndGet());
        String uniqName = mkName(name);

        record.setName(uniqName);
        record.setCode(uniqName);
        record.setDescription(uniqName);

        record.insert();

        return record.getId();
    }

}
