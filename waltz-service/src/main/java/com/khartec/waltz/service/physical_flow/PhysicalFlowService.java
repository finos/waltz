package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalFlowService {

    private final PhysicalFlowDao physicalFlowDao;


    @Autowired
    public PhysicalFlowService(PhysicalFlowDao physicalDataFlowDao) {
        checkNotNull(physicalDataFlowDao, "physicalFlowDao cannot be null");
        this.physicalFlowDao = physicalDataFlowDao;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByEntityReference(ref);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return physicalFlowDao.findBySpecificationId(specificationId);
    }


    public PhysicalFlow getById(long id) {
        return physicalFlowDao.getById(id);
    }
}
