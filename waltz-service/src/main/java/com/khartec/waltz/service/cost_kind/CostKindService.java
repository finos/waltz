package com.khartec.waltz.service.cost_kind;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.cost.CostKindDao;
import com.khartec.waltz.model.cost.EntityCostKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CostKindService {

    private final CostKindDao costKindDao;

    @Autowired
    CostKindService(CostKindDao costKindDao){

        Checks.checkNotNull(costKindDao, "costKindDao must not be null.");
        this.costKindDao = costKindDao;
    }

    public Set<EntityCostKind> findAll(){
        return costKindDao.findAll();
    }
}
