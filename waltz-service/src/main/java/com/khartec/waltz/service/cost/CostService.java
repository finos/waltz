package com.khartec.waltz.service.cost;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.cost.CostDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.EntityCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptySet;

@Service
public class CostService {

    private final CostDao costDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    CostService(CostDao costDao){
        Checks.checkNotNull(costDao, "costDao must not be null");

        this.costDao = costDao;
    }


    public Set<EntityCost> findByEntityReference(EntityReference ref){
        return costDao.findByEntityReference(ref);
    }


    public Set<EntityCost> findBySelector(Long costKindId,
                                          IdSelectionOptions selectionOptions,
                                          EntityKind targetKind){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return costDao.findLatestYear()
                .map(year -> costDao.findByCostKindIdAndSelectorForYear(costKindId, genericSelector,  year))
                .orElse(emptySet());
    }


    public Set<EntityCost> findBySelector(IdSelectionOptions selectionOptions,
                                          EntityKind targetKind){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return costDao.findLatestYear()
                .map(year -> costDao.findBySelectorForYear(genericSelector, year))
                .orElse(emptySet());
    }


    public Set<EntityCost> findByCostKindAndSelector(Long costKindId,
                                                     IdSelectionOptions selectionOptions,
                                                     EntityKind targetKind){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return costDao.findLatestYear()
                .map(year -> costDao.findByCostKindIdAndSelectorForYear(costKindId, genericSelector,  year))
                .orElse(emptySet());
    }
}
