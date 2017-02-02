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

package com.khartec.waltz.service.physical_flow_lineage;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.physical_flow_lineage.PhysicalFlowLineageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.attestation.AttestationType;
import com.khartec.waltz.model.attestation.ImmutableAttestation;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageAddCommand;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageRemoveCommand;
import com.khartec.waltz.service.attestation.AttestationService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalFlowLineageService {

    private final AttestationService attestationService;
    private final PhysicalFlowLineageDao physicalFlowLineageDao;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public PhysicalFlowLineageService(AttestationService attestationService,
                                      ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                      PhysicalFlowLineageDao physicalFlowLineageDao) {
        checkNotNull(attestationService, "attestationService cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(physicalFlowLineageDao, "physicalFlowLineageDao cannot be null");

        this.attestationService = attestationService;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.physicalFlowLineageDao = physicalFlowLineageDao;
    }


    public Collection<PhysicalFlowLineage> findByPhysicalFlowId(long id) {
        return physicalFlowLineageDao.findByPhysicalFlowId(id);
    }


    public Collection<PhysicalFlowLineage> findContributionsByPhysicalFlowId(long id) {
        return physicalFlowLineageDao.findContributionsByPhysicalFlowId(id);
    }


    public Collection<PhysicalFlowLineage> findAllLineageReports() {
        return physicalFlowLineageDao.findAllLineageReports();
    }


    public Collection<PhysicalFlowLineage> findLineageReportsByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return physicalFlowLineageDao.findLineageReportsByAppIdSelector(selector);
    }


    public CommandResponse<PhysicalFlowLineageRemoveCommand> removeContribution(PhysicalFlowLineageRemoveCommand removeCommand) {
        checkNotNull(removeCommand, "removeCommand cannot be null");
        return physicalFlowLineageDao.removeContribution(removeCommand);
    }


    public CommandResponse<PhysicalFlowLineageAddCommand> addContribution(PhysicalFlowLineageAddCommand addCommand) {
        checkNotNull(addCommand, "addCommand cannot be null");
        CommandResponse<PhysicalFlowLineageAddCommand> response = physicalFlowLineageDao.addContribution(addCommand);

        ImmutableAttestation described = ImmutableAttestation.builder()
                .entityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, addCommand.describedFlowId()))
                .attestationType(AttestationType.IMPLICIT)
                .attestedBy(addCommand.lastUpdate().by())
                .attestedAt(addCommand.lastUpdate().at())
                .comments("Implied by lineage creation")
                .build();

        ImmutableAttestation contributing = ImmutableAttestation.copyOf(described)
                .withEntityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, addCommand.contributingFlowId()));

        attestationService.create(described, addCommand.lastUpdate().by());
        attestationService.create(contributing, addCommand.lastUpdate().by());

        return response;
    }
}
