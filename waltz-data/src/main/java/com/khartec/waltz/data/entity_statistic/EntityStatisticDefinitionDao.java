package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.StatisticCategory;
import com.khartec.waltz.model.StatisticType;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatisticDefinition;
import com.khartec.waltz.schema.tables.records.EntityStatisticDefinitionRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticDefinitionDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition esd = ENTITY_STATISTIC_DEFINITION.as("esd");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

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

        record.setId(domainObj.id().get());
        record.setParentId(domainObj.parentId().orElse(null));
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
        return dsl.select(esd.fields())
                .from(esd)
                .fetch(TO_DEFINITION_MAPPER);
    }


    public List<EntityStatisticDefinition> findTopLevelDefinitions() {
        return dsl.select(esd.fields())
                .from(esd)
                .where(esd.PARENT_ID.isNull())
                .and(esd.ACTIVE.eq(Boolean.TRUE))
                .fetch(TO_DEFINITION_MAPPER);
    }


    public List<EntityStatisticDefinition> findRelated(long id) {
        Condition findSelf = esd.ID.eq(id);
        Condition findChildren = esd.PARENT_ID.eq(id);

        SelectConditionStep<Record1<Long>> parentIdSelector = dsl
                .select(esd.PARENT_ID)
                .from(esd)
                .where(findSelf);

        Condition findParent = esd.ID.eq(parentIdSelector);
        Condition findSiblings = esd.PARENT_ID.eq(parentIdSelector);

        return dsl.select(esd.fields())
                .from(esd)
                .where(findChildren
                        .or(findSelf)
                        .or(findParent)
                        .or(findSiblings)
                )
                .and(esd.ACTIVE.eq(Boolean.TRUE))
                .fetch(TO_DEFINITION_MAPPER);
    }


    private List<EntityStatisticDefinition> find(SelectConditionStep<Record1<Long>> statSelector,
                                              Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        // aggregate query
        Condition condition = esd.ACTIVE.eq(true)
                .and(esd.ID.in(statSelector))
                .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(esv.ENTITY_ID.in(appIdSelector))
                .and(esv.CURRENT.eq(true));


        // combine with definitions
        return dsl.selectDistinct(esd.fields())
                .from(esd)
                .join(esv)
                .on(esd.ID.eq(esv.STATISTIC_ID))
                .where(dsl.renderInlined(condition))
                .fetch(TO_DEFINITION_MAPPER);
    }

}
