/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.Collections.emptyList;
import static org.jooq.impl.DSL.*;

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


    public boolean isUsed(long id) {
        Field<Boolean> specUsed = DSL.when(
                    exists(selectFrom(PHYSICAL_FLOW).where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(id))),
                    val(true))
                .otherwise(false).as("spec_used");

        return dsl.select(specUsed)
                .fetchOne(specUsed);

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


    public Long create(PhysicalSpecification specification) {
        checkNotNull(specification, "specification cannot be null");
        checkFalse(specification.id().isPresent(), "specification must not have an id");

        PhysicalSpecificationRecord record = dsl.newRecord(PHYSICAL_SPECIFICATION);
        record.setOwningEntityKind(specification.owningEntity().kind().name());
        record.setOwningEntityId(specification.owningEntity().id());

        record.setName(specification.name());
        record.setExternalId(specification.externalId().orElse(""));
        record.setDescription(specification.description());
        record.setFormat(specification.format().name());
        record.setProvenance("waltz");

        record.store();
        return record.getId();
    }


    public int delete(long specId) {
        return dsl.deleteFrom(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.eq(specId))
                .and(notExists(selectFrom(PHYSICAL_FLOW)
                                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specId))))
                .execute();
    }

}
