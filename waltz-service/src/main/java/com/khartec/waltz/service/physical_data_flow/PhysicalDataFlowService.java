package com.khartec.waltz.service.physical_data_flow;

import com.khartec.waltz.data.physical_data_flow.PhysicalDataFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_data_flow.PhysicalDataFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalDataFlowService {

    private final PhysicalDataFlowDao physicalDataFlowDao;


    @Autowired
    public PhysicalDataFlowService(PhysicalDataFlowDao physicalDataFlowDao) {
        checkNotNull(physicalDataFlowDao, "physicalDataFlowDao cannot be null");
        this.physicalDataFlowDao = physicalDataFlowDao;
    }


    public List<PhysicalDataFlow> findFlowsForEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalDataFlowDao.findFlowsForEntityReference(ref);
    }
}
