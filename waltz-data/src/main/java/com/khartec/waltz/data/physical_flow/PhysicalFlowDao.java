package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.EntityNameUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Repository
public class PhysicalFlowDao {


    public static final Field<String> targetEntityNameField = EntityNameUtilities.mkEntityNameField(
            PHYSICAL_FLOW.TARGET_ENTITY_ID,
            PHYSICAL_FLOW.TARGET_ENTITY_KIND,
            ListUtilities.newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR)).as("target_name_field");


    public static final RecordMapper<Record, PhysicalFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowRecord record = r.into(PHYSICAL_FLOW);
        return ImmutablePhysicalFlow.builder()
                .id(record.getId())
                .provenance(record.getProvenance())
                .specificationId(record.getSpecificationId())
                .basisOffset(record.getBasisOffset())
                .frequency(FrequencyKind.valueOf(record.getFrequency()))
                .description(record.getDescription())
                .target(
                        EntityReference.mkRef(
                                EntityKind.valueOf(record.getTargetEntityKind()),
                                record.getTargetEntityId(),
                                r.getValue(targetEntityNameField)))
                .transport(TransportKind.valueOf(record.getTransport()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {

        checkNotNull(ref, "ref cannot be null");

        Condition isTarget = PHYSICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name())
                .and(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id()));

        Condition isSource = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(PHYSICAL_FLOW.TARGET_ENTITY_ID))
                .where(isTarget.or(isSource))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public PhysicalFlow getById(long id) {
        return findByCondition(PHYSICAL_FLOW.ID.eq(id))
                .stream()
                .findFirst()
                .orElse(null);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return findByCondition(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specificationId));
    }


    public List<PhysicalFlow> findBySelector(Select<Record1<Long>> selector) {
        return findByCondition(PHYSICAL_FLOW.ID.in(selector));
    }


    private List<PhysicalFlow> findByCondition(Condition condition) {

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findAllDescribedLineageFlows() {

        return dsl.selectDistinct(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.in(
                        DSL.selectDistinct(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID).from(PHYSICAL_FLOW_LINEAGE)))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
