package com.khartec.waltz.service.cost_kind;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.cost.CostKindDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.EntityCostKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CostKindService {

    private final CostKindDao costKindDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    CostKindService(CostKindDao costKindDao){

        Checks.checkNotNull(costKindDao, "costKindDao must not be null.");
        this.costKindDao = costKindDao;
    }


    public Set<EntityCostKind> findAll(){
        return costKindDao.findAll();
    }


    public Set<EntityCostKind> findExistingCostIdsBySelectorRoute(EntityKind targetKind, IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);
        return costKindDao.findExistingCostKindsBySelector(genericSelector);
    }
}
