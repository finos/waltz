package com.khartec.waltz.data.data_type_usage;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.data_type_usage.ImmutableDataTypeUsage;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.usage_info.ImmutableUsageInfo;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.schema.tables.records.DataTypeUsageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.limit;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;

@Repository
public class DataTypeUsageDao {

    private static final RecordMapper<Record, DataTypeUsage> TO_USAGE_MAPPER = r -> {
        DataTypeUsageRecord record = r.into(DATA_TYPE_USAGE);
        return ImmutableDataTypeUsage.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .build())
                .dataTypeCode(record.getDataTypeCode())
                .usage(ImmutableUsageInfo.builder()
                        .kind(UsageKind.valueOf(record.getUsageKind()))
                        .description(record.getDescription())
                        .isSelected(record.getIsSelected())
                        .build())
                .provenance(record.getProvenance())
                .build();
    };


    private static final RecordMapper<Record, UsageInfo> TO_USAGE_INFO_MAPPER = r -> {
        DataTypeUsageRecord record = r.into(DATA_TYPE_USAGE);
        return ImmutableUsageInfo.builder()
                .kind(UsageKind.valueOf(record.getUsageKind()))
                .description(record.getDescription())
                .isSelected(record.getIsSelected())
                .build();
    };


    private static final Function<DataTypeUsage, DataTypeUsageRecord> TO_RECORD_MAPPER = domain -> {
        DataTypeUsageRecord record = new DataTypeUsageRecord();
        record.setEntityKind(domain.entityReference().kind().name());
        record.setEntityId(domain.entityReference().id());
        record.setDataTypeCode(domain.dataTypeCode());
        record.setProvenance(domain.provenance());
        record.setUsageKind(domain.usage().kind().name());
        record.setDescription(
                limit(
                    domain.usage().description(),
                    DATA_TYPE_USAGE.DESCRIPTION.getDataType().length()));
        record.setIsSelected(domain.usage().isSelected());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public DataTypeUsageDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DataTypeUsage> findForIdSelector(EntityKind kind, Select<Record1<Long>> selector) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(selector, "selector cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.in(selector)));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name())
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id())));
    }


    public List<Tally<String>> findUsageStatsForDataTypeSelector(Select<Record1<Long>> dataTypeIdSelector) {
        SelectConditionStep<Record1<String>> codeSelector = DSL.select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.in(dataTypeIdSelector));

        return dsl.select(DATA_TYPE_USAGE.USAGE_KIND, DSL.count())
                .from(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.DATA_TYPE_CODE.in(codeSelector))
                .groupBy(DATA_TYPE_USAGE.USAGE_KIND)
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }


    public List<DataTypeUsage> findForDataTypeSelector(Select<Record1<Long>> dataTypeIdSelector) {
        checkNotNull(dataTypeIdSelector, "dataTypeIdSelector cannot be null");

        SelectConditionStep<Record1<String>> codeSelector = DSL.select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.in(dataTypeIdSelector));

        return findByCondition(DATA_TYPE_USAGE.DATA_TYPE_CODE.in(codeSelector));
    }


    public List<DataTypeUsage> findForEntityAndDataType(EntityReference ref, String dataTypeCode) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(dataTypeCode, "dataTypeCode cannot be null");

        return dsl.select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                        .and(DATA_TYPE_USAGE.DATA_TYPE_CODE.eq(dataTypeCode)))
                .fetch(TO_USAGE_MAPPER);
    }


    public int deleteForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl.deleteFrom(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                .execute();
    }


    private List<DataTypeUsage> findByCondition(Condition condition) {
        checkNotNull(condition, "condition cannot be null");
        return dsl.select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .where(dsl.renderInlined(condition))
                .fetch(TO_USAGE_MAPPER);
    }


    public int[] insertUsageInfo(EntityReference ref,
                                 String dataTypeCode,
                                 List<UsageInfo> inserts) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(dataTypeCode, "dataTypeCode cannot be null");
        checkNotNull(inserts, "inserts cannot be null");

        Set<DataTypeUsageRecord> records = toRecords(ref, dataTypeCode, inserts);

        return dsl
                .batchInsert(records)
                .execute();
    }


    public int deleteUsageInfo(EntityReference ref,
                               String dataTypeCode,
                               List<UsageKind> deletes) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(dataTypeCode, "dataTypeCode cannot be null");
        checkNotNull(deletes, "deletes cannot be null");

        return dsl.deleteFrom(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                .and(DATA_TYPE_USAGE.DATA_TYPE_CODE.eq(dataTypeCode))
                .and(DATA_TYPE_USAGE.USAGE_KIND.in(deletes))
                .execute();
    }


    public int[] updateUsageInfo(EntityReference ref,
                                 String dataTypeCode,
                                 List<UsageInfo> updates) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(dataTypeCode, "dataTypeCode cannot be null");
        checkNotNull(updates, "updates cannot be null");

        Set<DataTypeUsageRecord> records = toRecords(ref, dataTypeCode, updates);

        return dsl
                .batchUpdate(records)
                .execute();
    }


    private Set<DataTypeUsageRecord> toRecords(EntityReference ref, String dataTypeCode, List<UsageInfo> usages) {
        ImmutableDataTypeUsage.Builder recordBuilder = ImmutableDataTypeUsage.builder()
                .provenance("waltz")
                .entityReference(ref)
                .dataTypeCode(dataTypeCode);

        return usages.stream()
                .map(i -> recordBuilder.usage(i).build())
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toSet());
    }
}
