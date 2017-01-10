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

package com.khartec.waltz.data.physical_flow_lineage;


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
import com.khartec.waltz.schema.tables.records.PhysicalFlowLineageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.physical_flow.PhysicalFlowDao.targetEntityNameField;
import static com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;

@Repository
public class PhysicalFlowLineageDao {


    private static final RecordMapper<Record, PhysicalFlowLineage> TO_DOMAIN_MAPPER = r ->
            ImmutablePhysicalFlowLineage.builder()
                    .sourceEntity(EntityReference.mkRef(
                            EntityKind.valueOf(r.getValue(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND)),
                            r.getValue(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID),
                            r.getValue(owningEntityNameField)))
                    .targetEntity(EntityReference.mkRef(
                            EntityKind.valueOf(r.getValue(PHYSICAL_FLOW.TARGET_ENTITY_KIND)),
                            r.getValue(PHYSICAL_FLOW.TARGET_ENTITY_ID),
                            r.getValue(targetEntityNameField)))
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
        Condition condition = PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(id);
        return findContributionsByCondition(condition);
    }


    public Collection<PhysicalFlowLineage> findAllLineageReports() {
        Condition condition = PHYSICAL_FLOW.ID.in(
                DSL.selectDistinct(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID).from(PHYSICAL_FLOW_LINEAGE));

        return findDistinctLineageReportsByCondition(condition);
    }


    public Collection<PhysicalFlowLineage> findLineageReportsByAppIdSelector(Select<Record1<Long>> selector) {
        Condition condition = (
                PHYSICAL_FLOW.TARGET_ENTITY_ID.in(selector)
                        .and(PHYSICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .or(
                        (PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.in(selector)
                                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                );

        return findDistinctLineageReportsByCondition(condition);
    }


    public Collection<PhysicalFlowLineage> findByPhysicalFlowId(long id) {
        return dsl
                .select(PHYSICAL_FLOW_LINEAGE.fields())
                .select(owningEntityNameField)
                .select(targetEntityNameField)
                .select(PHYSICAL_FLOW.fields())
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_FLOW_LINEAGE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID))
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
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


    private Collection<PhysicalFlowLineage> findContributionsByCondition(Condition condition) {
        return dsl
                .select(PHYSICAL_FLOW_LINEAGE.fields())
                .select(owningEntityNameField)
                .select(targetEntityNameField)
                .select(PHYSICAL_FLOW.fields())
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_FLOW_LINEAGE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID))
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    private Collection<PhysicalFlowLineage> findDistinctLineageReportsByCondition(Condition condition) {
        return dsl
                .selectDistinct(PHYSICAL_FLOW.fields())
                .select(owningEntityNameField)
                .select(targetEntityNameField)
                .select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_FLOW_LINEAGE)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_LINEAGE.DESCRIBED_FLOW_ID))
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
