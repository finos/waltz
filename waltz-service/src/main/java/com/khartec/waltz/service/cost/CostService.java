package com.khartec.waltz.service.cost;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.cost.CostDao;
import com.khartec.waltz.data.cost.CostKindDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.EntityCost;
import com.khartec.waltz.model.cost.EntityCostsSummary;
import com.khartec.waltz.model.cost.ImmutableEntityCostsSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.first;
import static java.util.Optional.ofNullable;

@Service
public class CostService {

    private final CostDao costDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final CostKindDao costKindDao;

    @Autowired
    CostService(CostDao costDao, CostKindDao costKindDao){
        checkNotNull(costDao, "costDao must not be null");
        checkNotNull(costKindDao, "costKindDao must not be null");

        this.costKindDao = costKindDao;
        this.costDao = costDao;
    }


    public Set<EntityCost> findByEntityReference(EntityReference ref){
        return costDao.findByEntityReference(ref);
    }


    public Set<EntityCost> findBySelector(IdSelectionOptions selectionOptions,
                                          EntityKind targetKind){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return costDao.findBySelectorForYear(genericSelector);
    }


    public EntityCostsSummary summariseByCostKindAndSelector(Long costKindId,
                                                             IdSelectionOptions selectionOptions,
                                                             EntityKind targetKind,
                                                             int limit){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        Set<EntityCost> topCosts = costDao.findTopCostsForCostKindAndSelector(
                costKindId,
                genericSelector,
                limit);

        Integer year = ofNullable(first(topCosts))
                .map(EntityCost::year)
                .orElse(LocalDate.now().getYear());

        costDao.getMappedAndMissingCountsForKindAndYearBySelector(costKindId, year, genericSelector);

        return ImmutableEntityCostsSummary.builder()
                .costKind(costKindDao.getById(costKindId))
                .year(year)
                .total(costDao.getTotalForKindAndYearBySelector(costKindId, year, genericSelector))
                .topCosts(topCosts)
                .mappedCount(70)
                .missingCount(3)
                .build();
    }
}
