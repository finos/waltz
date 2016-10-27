package com.khartec.waltz.service.physical_flow_lineage;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.physical_flow_lineage.PhysicalFlowLineageDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageAddCommand;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineageRemoveCommand;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalFlowLineageService {

    private final PhysicalFlowLineageDao physicalFlowLineageDao;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public PhysicalFlowLineageService(ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                      PhysicalFlowLineageDao physicalFlowLineageDao) {
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(physicalFlowLineageDao, "physicalFlowLineageDao cannot be null");

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
        return physicalFlowLineageDao.addContribution(addCommand);
    }
}
