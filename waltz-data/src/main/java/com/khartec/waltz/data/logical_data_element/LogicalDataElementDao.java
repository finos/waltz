package com.khartec.waltz.data.logical_data_element;

import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.FieldDataType;
import com.khartec.waltz.model.logical_data_element.ImmutableLogicalDataElement;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import com.khartec.waltz.schema.tables.records.LogicalDataElementRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.LOGICAL_DATA_ELEMENT;


@Repository
public class LogicalDataElementDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalDataElementDao.class);

    public static final RecordMapper<Record, LogicalDataElement> TO_DOMAIN_MAPPER = r -> {
        LogicalDataElementRecord record = r.into(LOGICAL_DATA_ELEMENT);
        LogicalDataElement element = ImmutableLogicalDataElement.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .name(record.getName())
                .description(record.getDescription())
                .type(FieldDataType.valueOf(record.getType()))
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getEntityLifecycleStatus()))
                .provenance(record.getProvenance())
                .build();

        return element;
    };

    private final DSLContext dsl;


    @Autowired
    public LogicalDataElementDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public LogicalDataElement getById(long id) {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public LogicalDataElement getByExternalId(String externalId) {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LogicalDataElement> findAll() {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LogicalDataElement> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .selectFrom(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
