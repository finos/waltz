package com.khartec.waltz.data.data_type_usage;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.data_type_usage.ImmutableDataTypeUsage;
import com.khartec.waltz.model.data_type_usage.UsageKind;
import com.khartec.waltz.schema.tables.records.DataTypeUsageRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.StringUtilities.limit;
import static com.khartec.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;

@Repository
public class DataTypeUsageDao {

    public static final RecordMapper<Record, DataTypeUsage> TO_DOMAIN_MAPPER = r -> {
        DataTypeUsageRecord record = r.into(DATA_TYPE_USAGE);
        return ImmutableDataTypeUsage.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .build())
                .dataTypeCode(record.getDataTypeCode())
                .usageKind(UsageKind.valueOf(record.getUsageKind()))
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .build();
    };


    private static final Function<DataTypeUsage, DataTypeUsageRecord> TO_RECORD_MAPPER = domain -> {
        DataTypeUsageRecord record = new DataTypeUsageRecord();
        record.setEntityKind(domain.entityReference().kind().name());
        record.setEntityId(domain.entityReference().id());
        record.setDataTypeCode(domain.dataTypeCode());
        record.setUsageKind(domain.usageKind().name());
        record.setDescription(limit(domain.description(), DATA_TYPE_USAGE.DESCRIPTION.getDataType().length()));
        record.setProvenance(domain.provenance());
        return record;
    };

    private final DSLContext dsl;


    @Autowired
    public DataTypeUsageDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DataTypeUsage> findForIdSelector(EntityKind kind, Select<Record1<Long>> selector) {
        Checks.checkNotNull(kind, "kind cannot be null");
        Checks.checkNotNull(selector, "selector cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.in(selector)));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        Checks.checkNotNull(ref, "ref cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name())
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id())));
    }


    public List<DataTypeUsage> findForDataType(String dataTypeCode) {
        Checks.checkNotEmptyString(dataTypeCode, "dataTypeCode cannot be empty");
        return findByCondition(DATA_TYPE_USAGE.DATA_TYPE_CODE.eq(dataTypeCode));
    }


    public int[] save(List<DataTypeUsage> dataTypeUsages) {
        Checks.checkNotNull(dataTypeUsages, "dataTypeUsages cannot be null");
        List<DataTypeUsageRecord> records = ListUtilities.map(dataTypeUsages, TO_RECORD_MAPPER);
        return dsl.batchInsert(records).execute();
    }


    public int deleteForEntity(EntityReference ref) {
        Checks.checkNotNull(ref, "ref cannot be null");

        return dsl.deleteFrom(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                .execute();
    }


    private List<DataTypeUsage> findByCondition(Condition condition) {
        Checks.checkNotNull(condition, "condition cannot be null");
        return dsl.select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DOMAIN_MAPPER);
    }



}
