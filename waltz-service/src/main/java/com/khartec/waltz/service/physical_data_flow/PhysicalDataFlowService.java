package com.khartec.waltz.service.physical_data_flow;

import com.khartec.waltz.data.physical_data_flow.PhysicalDataFlowDao;
import com.khartec.waltz.data.physical_data_flow.PhysicalDataFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.physical_data_flow.PhysicalDataFlow;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalDataFlowService {

    private final PhysicalDataFlowDao physicalDataFlowDao;
    private final PhysicalDataFlowIdSelectorFactory idSelectorFactory;


    @Autowired
    public PhysicalDataFlowService(PhysicalDataFlowDao physicalDataFlowDao,
                                   PhysicalDataFlowIdSelectorFactory idSelectorFactory) {
        checkNotNull(physicalDataFlowDao, "physicalDataFlowDao cannot be null");
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");
        this.physicalDataFlowDao = physicalDataFlowDao;
        this.idSelectorFactory = idSelectorFactory;
    }


    public List<PhysicalDataFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalDataFlowDao.findByEntityReference(ref);
    }


    public List<PhysicalDataFlow> findByArticleId(long articleId) {
        return physicalDataFlowDao.findByArticleId(articleId);
    }

    public Collection<PhysicalDataFlow> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return physicalDataFlowDao.findBySelector(selector);
    }
}
