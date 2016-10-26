package com.khartec.waltz.service.physical_flow_lineage;

import com.khartec.waltz.data.physical_flow_lineage.PhysicalFlowLineageDao;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageAddCommand;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageRemoveCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalFlowLineageService {

    private final PhysicalFlowLineageDao physicalFlowLineageDao;


    @Autowired
    public PhysicalFlowLineageService(PhysicalFlowLineageDao physicalFlowLineageDao) {
        checkNotNull(physicalFlowLineageDao, "physicalFlowLineageDao cannot be null");

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


    public CommandResponse<PhysicalFlowLineageRemoveCommand> removeContribution(PhysicalFlowLineageRemoveCommand removeCommand) {
        checkNotNull(removeCommand, "removeCommand cannot be null");
        return physicalFlowLineageDao.removeContribution(removeCommand);
    }


    public CommandResponse<PhysicalFlowLineageAddCommand> addContribution(PhysicalFlowLineageAddCommand addCommand) {
        checkNotNull(addCommand, "addCommand cannot be null");
        return physicalFlowLineageDao.addContribution(addCommand);
    }
}
