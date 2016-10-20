package com.khartec.waltz.data.physical_flow_lineage;


import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.physical_flow_lineage.ImmutablePhysicalFlowLineage;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageAddCommand;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageRemoveCommand;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.records.PhysicalFlowLineageRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;

@Repository
public class PhysicalFlowLineageDao {

    private static final Application sourceApp = Application.APPLICATION.as("sourceApp");
    private static final Application targetApp = Application.APPLICATION.as("targetApp");

    private static final RecordMapper<Record, PhysicalFlowLineage> TO_DOMAIN_MAPPER = r ->
            ImmutablePhysicalFlowLineage.builder()
                    .sourceApplication(ApplicationDao.TO_DOMAIN_MAPPER.map(r.into(sourceApp)))
                    .targetApplication(ApplicationDao.TO_DOMAIN_MAPPER.map(r.into(targetApp)))
                    .flow(PhysicalFlowDao.TO_DOMAIN_MAPPER.map(r))
                    .specification(PhysicalSpecificationDao.TO_DOMAIN_MAPPER.map(r))
                    .description(r.getValue(PHYSICAL_FLOW_LINEAGE.DESCRIPTION))
                    .build();


    private final DSLContext dsl;


    @Autowired
    public PhysicalFlowLineageDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<PhysicalFlowLineage> findContributionsByPhysicalFlowId(long id) {
        return dsl
                .select(PHYSICAL_FLOW_LINEAGE.fields())
                .select(sourceApp.fields())
                .select(targetApp.fields())
                .select(PHYSICAL_FLOW.fields())
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_FLOW_LINEAGE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID))
                .innerJoin(targetApp)
                .on(targetApp.ID.eq(PHYSICAL_FLOW.TARGET_ENTITY_ID))
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(sourceApp)
                .on(sourceApp.ID.eq(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID))
                .where(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<PhysicalFlowLineage> findByPhysicalFlowId(long id) {
        return dsl
                .select(PHYSICAL_FLOW_LINEAGE.fields())
                .select(sourceApp.fields())
                .select(targetApp.fields())
                .select(PHYSICAL_FLOW.fields())
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_FLOW_LINEAGE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID))
                .innerJoin(targetApp)
                .on(targetApp.ID.eq(PHYSICAL_FLOW.TARGET_ENTITY_ID))
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(sourceApp)
                .on(sourceApp.ID.eq(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID))
                .where(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public CommandResponse<PhysicalFlowLineageRemoveCommand> removeContribution(PhysicalFlowLineageRemoveCommand removeCommand) {
        int rc = dsl.deleteFrom(PHYSICAL_FLOW_LINEAGE)
                .where(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID.eq(removeCommand.describedFlowId()))
                .and(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(removeCommand.contributingFlowId()))
                .execute();

        return ImmutableCommandResponse.<PhysicalFlowLineageRemoveCommand>builder()
                .outcome(rc == 1 ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .originalCommand(removeCommand)
                .entityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, removeCommand.describedFlowId()))
                .build();
    }


    public CommandResponse<PhysicalFlowLineageAddCommand> addContribution(PhysicalFlowLineageAddCommand addCommand) {
        PhysicalFlowLineageRecord record = dsl.newRecord(PHYSICAL_FLOW_LINEAGE);

        record.setDescription(addCommand.description());
        record.setContributorFlowId(addCommand.contributingFlowId());
        record.setDescribedFlowId(addCommand.describedFlowId());
        record.setLastUpdatedBy(addCommand.lastUpdate().by());

        int rc = record.store();

        return ImmutableCommandResponse.<PhysicalFlowLineageAddCommand>builder()
                .outcome(rc == 1 ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .originalCommand(addCommand)
                .entityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, addCommand.describedFlowId()))
                .build();

    }
}
