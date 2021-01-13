package com.khartec.waltz.service.cost;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.cost.CostDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.cost.EntityCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CostService {

    private final CostDao costDao;

    @Autowired
    CostService(CostDao costDao){

        Checks.checkNotNull(costDao, "costDao must not be null.");
        this.costDao = costDao;
    }

    public Set<EntityCost> findByEntityReference(EntityReference ref){
        return costDao.findByEntityReference(ref);
    }
}
