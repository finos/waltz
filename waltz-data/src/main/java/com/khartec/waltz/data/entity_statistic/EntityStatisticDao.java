package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_statistic.EntityStatistic;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatistic;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition es = ENTITY_STATISTIC_DEFINITION.as("es");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");
    private static final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");

    private final DSLContext dsl;


    private static final RecordMapper<? super Record, EntityStatistic> TO_COMPOUND_MAPPER = record
            -> ImmutableEntityStatistic.builder()
            .definition(EntityStatisticDefinitionDao.TO_DEFINITION_MAPPER.map(record))
            .value(EntityStatisticValueDao.TO_VALUE_MAPPER.map(record))
            .build();


    @Autowired
    public EntityStatisticDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.select(es.fields())
                .select(esv.fields())
                .from(es)
                .innerJoin(esv)
                .on(esv.STATISTIC_ID.eq(es.ID))
                .where(es.ACTIVE.eq(active)
                        .and(esv.ENTITY_KIND.eq(ref.kind().name()))
                        .and(esv.ENTITY_ID.eq(ref.id()))
                        .and(esv.CURRENT.eq(true)))
                .fetch(TO_COMPOUND_MAPPER);
    }


}
