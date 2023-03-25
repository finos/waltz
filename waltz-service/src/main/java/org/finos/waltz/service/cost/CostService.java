/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.cost;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.cost.CostDao;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.cost.EntityCost;
import org.finos.waltz.model.cost.EntityCostsSummary;
import org.finos.waltz.model.cost.ImmutableEntityCostsSummary;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.finos.waltz.common.FunctionUtilities.time;

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

        return costDao.findBySelector(genericSelector);
    }


    public EntityCostsSummary summariseByCostKindAndSelector(Long costKindId,
                                                             IdSelectionOptions selectionOptions,
                                                             EntityKind targetKind,
                                                             int year,
                                                             int limit){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        Set<EntityCost> topCosts = time(
                "topCosts: "+selectionOptions.entityReference(),
                () -> costDao.findTopCostsForCostKindAndSelector(
                        costKindId,
                        year,
                        genericSelector,
                        limit));


        BigDecimal totalCost = time(
                "totalCosts: "+selectionOptions.entityReference(),
                () -> costDao.getTotalForKindAndYearBySelector(
                        costKindId,
                        year,
                        genericSelector));

        Tuple2<Integer, Integer> mappedAndMissingCounts = time(
                "missingCosts: "+selectionOptions.entityReference(),
                () -> costDao.getMappedAndMissingCountsForKindAndYearBySelector(
                        costKindId,
                        year,
                        genericSelector));

        return ImmutableEntityCostsSummary
                .builder()
                .costKind(costKindDao.getById(costKindId))
                .year(year)
                .total(ofNullable(totalCost).orElse(BigDecimal.ZERO))
                .topCosts(topCosts)
                .mappedCount(mappedAndMissingCounts.v1)
                .missingCount(mappedAndMissingCounts.v2)
                .build();
    }

}
