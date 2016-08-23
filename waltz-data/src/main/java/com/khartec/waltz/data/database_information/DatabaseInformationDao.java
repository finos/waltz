package com.khartec.waltz.data.database_information;

import com.khartec.waltz.common.ArrayBuilder;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import com.khartec.waltz.model.database_information.ImmutableDatabaseInformation;
import com.khartec.waltz.model.database_information.ImmutableDatabaseSummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.schema.tables.records.DatabaseInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class DatabaseInformationDao {

    private final DSLContext dsl;


    private final static RecordMapper<? super Record, DatabaseInformation> DATABASE_RECORD_MAPPER = r -> {
        DatabaseInformationRecord record = r.into(DATABASE_INFORMATION);
        return ImmutableDatabaseInformation.builder()
                .databaseName(record.getDatabaseName())
                .instanceName(record.getInstanceName())
                .environment(record.getEnvironment())
                .dbmsVendor(record.getDbmsVendor())
                .dbmsName(record.getDbmsName())
                .dbmsVersion(record.getDbmsVersion())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .assetCode(record.getAssetCode())
                .endOfLifeDate(record.getEndOfLifeDate())
                .build();
    };


    @Autowired
    public DatabaseInformationDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<DatabaseInformation> findByApplicationId(long id) {
        return dsl.select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .where(APPLICATION.ID.eq(id))
                .fetch(DATABASE_RECORD_MAPPER);
    }


    public Map<Long, List<DatabaseInformation>> findByAppSelector(Select<Record1<Long>> appIdSelector) {
        SelectField[] selectFields = new ArrayBuilder<SelectField>()
                .add(ENTITY_RELATIONSHIP.ID_A, ENTITY_RELATIONSHIP.KIND_A)
                .add(DATABASE_INFORMATION.fields())
                .build(new SelectField[]{});

        return dsl.select(selectFields)
                .from(DATABASE_INFORMATION)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .where(APPLICATION.ID.in(appIdSelector))
                .fetch()
                .stream()
                .map(r -> tuple(
                        r.getValue(ENTITY_RELATIONSHIP.ID_A),
                        DATABASE_RECORD_MAPPER.map(r)))
                .collect(groupingBy(
                        t -> t.v1(),
                        mapping(t -> t.v2(), Collectors.toList())
                ));
    }


    public DatabaseSummaryStatistics findStatsForAppSelector(Select<Record1<Long>> appIdSelector) {
        Table databaseInfo = DATABASE_INFORMATION
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .and(APPLICATION.ID.in(appIdSelector))
                .asTable();

        List<StringTally> vendorCounts = calculateStringTallies(
                dsl,
                databaseInfo,
                DATABASE_INFORMATION.DBMS_VENDOR,
                DSL.trueCondition());

        List<StringTally> environmentCounts = calculateStringTallies(
                dsl,
                databaseInfo,
                DATABASE_INFORMATION.ENVIRONMENT,
                DSL.trueCondition());

        return ImmutableDatabaseSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .environmentCounts(environmentCounts)
                .build();
    }

}
