package org.finos.waltz.service.cost_kind;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.cost.CostKindWithYears;
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


    public Set<CostKindWithYears> findAll(){
        return costKindDao.findAll();
    }


    public Set<CostKindWithYears> findCostKindsSelectorRoute(EntityKind targetKind,
                                                             IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(
                targetKind,
                selectionOptions);
        return costKindDao.findCostKindsBySelector(genericSelector);
    }
}
