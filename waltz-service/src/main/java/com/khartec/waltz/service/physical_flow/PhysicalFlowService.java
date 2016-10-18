package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalFlowService {

    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalFlowIdSelectorFactory idSelectorFactory;


    @Autowired
    public PhysicalFlowService(PhysicalFlowDao physicalDataFlowDao,
                               PhysicalFlowIdSelectorFactory idSelectorFactory) {
        checkNotNull(physicalDataFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");
        this.physicalFlowDao = physicalDataFlowDao;
        this.idSelectorFactory = idSelectorFactory;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByEntityReference(ref);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return physicalFlowDao.findBySpecificationId(specificationId);
    }

    public Collection<PhysicalFlow> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return physicalFlowDao.findBySelector(selector);
    }
}
