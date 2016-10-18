package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableProduceConsumeGroup;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.schema.tables.records.PhysicalSpecificationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.Collections.emptyList;

@Repository
public class PhysicalSpecificationDao {

    public static final RecordMapper<? super Record, PhysicalSpecification> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecificationRecord record = r.into(PHYSICAL_SPECIFICATION);
        return ImmutablePhysicalSpecification.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .owningApplicationId(record.getOwningApplicationId())
                .name(record.getName())
                .description(record.getDescription())
                .format(DataFormatKind.valueOf(record.getFormat()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecificationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalSpecification> findByProducerAppId(long appId) {
        return findByProducerAppIdQuery(appId)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecification> findByConsumerAppId(long appId) {
        return findByConsumerAppIdQuery(appId)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public ProduceConsumeGroup<PhysicalSpecification> findByAppId(long appId) {

        List<Tuple2<String, PhysicalSpecification>> results = findByProducerAppIdQuery(appId)
                .unionAll(findByConsumerAppIdQuery(appId))
                .fetch(r -> Tuple.tuple(
                        r.getValue("relationship", String.class),
                        TO_DOMAIN_MAPPER.map(r)));

        Map<String, Collection<PhysicalSpecification>> groupedResults = groupBy(
                t -> t.v1, // relationship
                t -> t.v2, // specification
                results);

        return ImmutableProduceConsumeGroup.<PhysicalSpecification>builder()
                .produces(groupedResults.getOrDefault("producer", emptyList()))
                .consumes(groupedResults.getOrDefault("consumer", emptyList()))
                .build();
    }


    public PhysicalSpecification getById(long id) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<PhysicalSpecification> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private Select<Record> findByProducerAppIdQuery(long appId) {
        return dsl
                .select(DSL.value("producer").as("relationship"))
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.OWNING_APPLICATION_ID.eq(appId));
    }


    private Select<Record> findByConsumerAppIdQuery(long appId) {
        return dsl
                .select(DSL.value("consumer").as("relationship"))
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(DATA_FLOW)
                .on(PHYSICAL_FLOW.FLOW_ID.eq(DATA_FLOW.ID))
                .where(DATA_FLOW.TARGET_ENTITY_ID.eq(appId))
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
    }

}
