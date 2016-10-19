package com.khartec.waltz.service.physical_flow_lineage;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.physical_flow_lineage.PhysicalFlowLineageDao;
import com.khartec.waltz.model.physical_flow_lineage.PhysicalFlowLineage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class PhysicalFlowLineageService {

    private final PhysicalFlowLineageDao physicalFlowLineageDao;

    @Autowired
    public PhysicalFlowLineageService(PhysicalFlowLineageDao physicalFlowLineageDao) {
        Checks.checkNotNull(physicalFlowLineageDao, "physicalFlowLineageDao cannot be null");
        this.physicalFlowLineageDao = physicalFlowLineageDao;
    }

    public Collection<PhysicalFlowLineage> findByPhysicalFlowId(long id) {
        return physicalFlowLineageDao.findByPhysicalFlowId(id);
    }

    public Collection<PhysicalFlowLineage> findContributionsByPhysicalFlowId(long id) {
        return physicalFlowLineageDao.findContributionsByPhysicalFlowId(id);
    }

}
