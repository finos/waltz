package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.StatisticCategory;
import com.khartec.waltz.model.StatisticType;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatisticDefinition;
import com.khartec.waltz.schema.tables.records.EntityStatisticDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticDefinitionDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition es = ENTITY_STATISTIC_DEFINITION.as("es");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");
    private static final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");

    public static final RecordMapper<? super Record, EntityStatisticDefinition> TO_DEFINITION_MAPPER = r -> {
        EntityStatisticDefinitionRecord record = r.into(ENTITY_STATISTIC_DEFINITION);

        return ImmutableEntityStatisticDefinition.builder()
                .id(record.getId())
                .name(record.getName())
                .description((record.getDescription()))
                .type(StatisticType.valueOf(record.getType()))
                .category(StatisticCategory.valueOf(record.getCategory()))
                .active(record.getActive())
                .renderer(record.getRenderer())
                .historicRenderer(record.getHistoricRenderer())
                .provenance(record.getProvenance())
                .parentId(Optional.ofNullable(record.getParentId()))
                .build();
    };


    private static final Function<EntityStatisticDefinition, EntityStatisticDefinitionRecord> TO_RECORD_MAPPER = domainObj -> {
        EntityStatisticDefinitionRecord record = new EntityStatisticDefinitionRecord();

        record.setName(domainObj.name());
        record.setDescription(domainObj.description());
        record.setType(domainObj.type().name());
        record.setCategory(domainObj.category().name());
        record.setActive(domainObj.active());
        record.setRenderer(domainObj.renderer());
        record.setHistoricRenderer(domainObj.historicRenderer());
        record.setProvenance(domainObj.provenance());

        return record;
    };





    private final DSLContext dsl;


    @Autowired
    public EntityStatisticDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public boolean insert(EntityStatisticDefinition entityStatistic) {
        checkNotNull(entityStatistic, "entityStatistic cannot be null");
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(entityStatistic)) == 1;
    }


    public List<EntityStatisticDefinition> getAllDefinitions() {
        return dsl.select(es.fields())
                .from(es)
                .fetch(TO_DEFINITION_MAPPER);
    }

}
