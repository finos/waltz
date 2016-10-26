package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
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
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.Collections.emptyList;

@Repository
public class PhysicalSpecificationDao {


    public static final Field<String> owningEntityNameField = mkEntityNameField(
                PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID,
                PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR))
            .as("owning_name_field");


    public static final RecordMapper<? super Record, PhysicalSpecification> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecificationRecord record = r.into(PHYSICAL_SPECIFICATION);
        return ImmutablePhysicalSpecification.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .owningEntity(EntityReference.mkRef(
                        EntityKind.valueOf(record.getOwningEntityKind()),
                        record.getOwningEntityId(),
                        r.getValue(owningEntityNameField)))
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


    public List<PhysicalSpecification> findByProducer(EntityReference ref) {
        return findByProducerEntityReferenceQuery(ref)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecification> findByConsumer(EntityReference ref) {
        return findByConsumerEntityReferenceQuery(ref)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public ProduceConsumeGroup<PhysicalSpecification> findByEntityReference(EntityReference ref) {

        List<Tuple2<String, PhysicalSpecification>> results = findByProducerEntityReferenceQuery(ref)
                .unionAll(findByConsumerEntityReferenceQuery(ref))
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
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<PhysicalSpecification> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecification> findForDescribedLineage() {

        return dsl.selectDistinct(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_FLOW).on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .where(PHYSICAL_FLOW.ID.in(
                        DSL.selectDistinct(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID).from(PHYSICAL_FLOW_LINEAGE)))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private Select<Record> findByProducerEntityReferenceQuery(EntityReference ref) {
        return dsl
                .select(DSL.value("producer").as("relationship"))
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id()))
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));
    }


    private Select<Record> findByConsumerEntityReferenceQuery(EntityReference ref) {
        return dsl
                .select(DSL.value("consumer").as("relationship"))
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .where(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id()))
                .and(PHYSICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()));
    }

}
