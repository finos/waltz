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
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;
import static org.jooq.impl.DSL.*;

@Repository
public class DataTypeUsageDao {

    private final com.khartec.waltz.schema.tables.DataType dt = DATA_TYPE.as("dt");
    private final com.khartec.waltz.schema.tables.DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");
    private final com.khartec.waltz.schema.tables.DataFlow df = DATA_FLOW.as("df");
    private final com.khartec.waltz.schema.tables.DataFlowDecorator dfd = DATA_FLOW_DECORATOR.as("dfd");
    private final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");


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


    public boolean recalculateForAllApplications() {

        dsl.transaction(configuration -> {
            DSLContext tx = DSL.using(configuration);

            Condition consumerDistributer = DATA_TYPE_USAGE.USAGE_KIND.in(UsageKind.CONSUMER.name(), UsageKind.DISTRIBUTOR.name());
            Condition bothApps = df.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                            .and(df.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

            // clear usages
            tx.deleteFrom(DATA_TYPE_USAGE)
                    .where(consumerDistributer)
                    .and(DATA_TYPE_USAGE.DESCRIPTION.eq(""))
                    .execute();

            // mark commented usages inactive
            tx.update(DATA_TYPE_USAGE)
                    .set(DATA_TYPE_USAGE.IS_SELECTED, false)
                    .where(consumerDistributer)
                    .and(DATA_TYPE_USAGE.DESCRIPTION.ne(""))
                    .execute();

            TableOnConditionStep<Record> flowsWithTypes = df.innerJoin(app)
                    .on(app.ID.eq(df.SOURCE_ENTITY_ID)
                            .or(app.ID.eq(df.TARGET_ENTITY_ID))
                            .and(bothApps))
                    .innerJoin(dfd)
                    .on(dfd.DATA_FLOW_ID.eq(df.ID))
                    .and(dfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                    .innerJoin(dt)
                    .on(dt.ID.eq(dfd.DECORATOR_ENTITY_ID));


            Field<String> caseConsDistr = DSL.when(df.SOURCE_ENTITY_ID.eq(app.ID), UsageKind.DISTRIBUTOR.name())
                    .otherwise(UsageKind.CONSUMER.name());

            Select<Record7<Long, String, String, String, String, String, Boolean>> usagesToInsert =
                    selectDistinct(
                            app.ID,
                            val(EntityKind.APPLICATION.name()),
                            dt.CODE,
                            caseConsDistr,
                            val(""),
                            val("waltz"),
                            val(true))
                        .from(flowsWithTypes)
                        .leftJoin(dtu)
                            .on(dtu.ENTITY_ID.eq(app.ID))
                            .and(dtu.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                            .and(dtu.DATA_TYPE_CODE.eq(dt.CODE))
                            .and(dtu.USAGE_KIND.eq(caseConsDistr))
                            .and(dtu.IS_SELECTED.eq(false))
                        .where(dt.CODE.ne("UNKNOWN"))
                            .and(dtu.ENTITY_ID.isNull());


            // insert new usages
            tx.insertInto(DATA_TYPE_USAGE)
                    .columns(
                            DATA_TYPE_USAGE.ENTITY_ID,
                            DATA_TYPE_USAGE.ENTITY_KIND,
                            DATA_TYPE_USAGE.DATA_TYPE_CODE,
                            DATA_TYPE_USAGE.USAGE_KIND,
                            DATA_TYPE_USAGE.DESCRIPTION,
                            DATA_TYPE_USAGE.PROVENANCE,
                            DATA_TYPE_USAGE.IS_SELECTED)
                    .select(usagesToInsert)
                    .execute();

            // update isSelected column
            tx.update(DATA_TYPE_USAGE)
                    .set(DATA_TYPE_USAGE.IS_SELECTED, true)
                    .where(DATA_TYPE_USAGE.IS_SELECTED.eq(false))
                    .and(exists(
                            selectFrom(flowsWithTypes)
                                .where(DATA_TYPE_USAGE.ENTITY_ID.eq(app.ID))
                                .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                                .and(DATA_TYPE_USAGE.DATA_TYPE_CODE.eq(dt.CODE))
                                .and(DATA_TYPE_USAGE.USAGE_KIND.eq(caseConsDistr))))
                    .execute();


        });

        return true;

    }


}
