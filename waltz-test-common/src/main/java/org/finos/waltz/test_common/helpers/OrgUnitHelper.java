package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;

@Service
public class OrgUnitHelper {

    protected static final AtomicLong counter = new AtomicLong(1_000_000);

    @Autowired
    DSLContext dsl;

    public Long createOrgUnit(String nameStem, Long parentId) {
        OrganisationalUnitRecord record = dsl.newRecord(ORGANISATIONAL_UNIT);
        record.setId(counter.incrementAndGet());
        record.setName(nameStem + " Name");
        record.setDescription(nameStem + " Desc");
        record.setParentId(parentId);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setProvenance("integration-test");
        record.insert();

        return record.getId();
    }
}
