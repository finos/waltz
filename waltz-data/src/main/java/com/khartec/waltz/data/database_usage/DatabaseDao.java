package com.khartec.waltz.data.database_usage;

import com.khartec.waltz.common.ArrayBuilder;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.database.Database;
import com.khartec.waltz.model.database.DatabaseSummaryStatistics;
import com.khartec.waltz.model.database.ImmutableDatabase;
import com.khartec.waltz.model.database.ImmutableDatabaseSummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.schema.tables.records.DatabaseRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.data.JooqUtilities.calculateTallies;
import static com.khartec.waltz.schema.tables.Database.DATABASE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class DatabaseDao {

    private final static String APP_KIND = EntityKind.APPLICATION.name();
    private final static String DB_KIND = EntityKind.DATABASE.name();

    private final DSLContext dsl;

    private final static RecordMapper<? super Record, Database> DATABASE_RECORD_MAPPER = r -> {
        DatabaseRecord record = r.into(DATABASE);
        return ImmutableDatabase.builder()
                .databaseName(record.getDatabaseName())
                .instanceName(record.getInstanceName())
                .environment(record.getEnvironment())
                .dbmsVendor(record.getDbmsVendor())
                .dbmsName(record.getDbmsName())
                .dbmsVersion(record.getDbmsVersion())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public DatabaseDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Database> findByApplicationId(long id) {

        Condition joinCondition = DSL.condition(true)
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(APP_KIND))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(id))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(DB_KIND))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(DATABASE.ID));

        return dsl.select(DATABASE.fields())
                .from(DATABASE)
                .innerJoin(ENTITY_RELATIONSHIP)
                .on(joinCondition)
                .fetch(DATABASE_RECORD_MAPPER);
    }


    public Map<Long, List<Database>> findByApplicationIds(List<Long> ids) {
        Condition joinCondition = byAppIdsJoinCondition(ids);

        SelectField[] selectFields = new ArrayBuilder<SelectField>()
                .add(ENTITY_RELATIONSHIP.ID_A, ENTITY_RELATIONSHIP.KIND_A)
                .add(DATABASE.fields())
                .build(new SelectField[]{});

        return dsl.select(selectFields)
                .from(DATABASE)
                .innerJoin(ENTITY_RELATIONSHIP)
                .on(joinCondition)
                .stream()
                .map(r -> tuple(
                        r.getValue(ENTITY_RELATIONSHIP.ID_A),
                        DATABASE_RECORD_MAPPER.map(r)))
                .collect(groupingBy(
                        t -> t.v1(),
                        mapping(t -> t.v2(), Collectors.toList())
                ));
    }

    private Condition byAppIdsJoinCondition(Collection<Long> ids) {
        return DSL.condition(true)
                    .and(ENTITY_RELATIONSHIP.KIND_A.eq(APP_KIND))
                    .and(ENTITY_RELATIONSHIP.ID_A.in(ids))
                    .and(ENTITY_RELATIONSHIP.KIND_B.eq(DB_KIND))
                    .and(ENTITY_RELATIONSHIP.ID_B.eq(DATABASE.ID));
    }

    public DatabaseSummaryStatistics findStatsForAppIds(Collection<Long> appIds) {
        Condition condition = byAppIdsJoinCondition(appIds);

        List<StringTally> vendorCounts = calculateTallies(
                dsl,
                DATABASE.innerJoin(ENTITY_RELATIONSHIP).on(condition),
                DATABASE.DBMS_VENDOR,
                DSL.trueCondition());

        List<StringTally> environmentCounts = calculateTallies(
                dsl,
                DATABASE.innerJoin(ENTITY_RELATIONSHIP).on(condition),
                DATABASE.ENVIRONMENT,
                DSL.trueCondition());

        return ImmutableDatabaseSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .environmentCounts(environmentCounts)
                .build();
    }


}
