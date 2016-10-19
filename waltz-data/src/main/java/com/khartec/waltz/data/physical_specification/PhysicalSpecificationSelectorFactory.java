package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Service
public class PhysicalSpecificationSelectorFactory implements IdSelectorFactory {

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case PHYSICAL_FLOW:
                return mkForPhysicalFlow(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForPhysicalFlow(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long flowId = options.entityReference().id();
        return DSL.select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(PHYSICAL_FLOW_LINEAGE)
                .on(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(PHYSICAL_FLOW.ID))
                .where(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID.eq(flowId)
                        .or(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(flowId)));
    }

}
