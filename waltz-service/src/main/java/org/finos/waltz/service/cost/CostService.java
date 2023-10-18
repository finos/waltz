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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.cost.AllocatedCostDefinitionDao;
import org.finos.waltz.data.cost.CostDao;
import org.finos.waltz.data.cost.CostKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.cost.AllocatedCostDefinition;
import org.finos.waltz.model.cost.CostKindWithYears;
import org.finos.waltz.model.cost.EntityCost;
import org.finos.waltz.model.cost.EntityCostsSummary;
import org.finos.waltz.model.cost.ImmutableEntityCostsSummary;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.indexBy;

@Service
public class CostService {

    private static final Logger LOG = LoggerFactory.getLogger(CostService.class);
    private final CostDao costDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final CostKindDao costKindDao;
    private final AllocatedCostDefinitionDao allocatedCostDefinitionDao;


    @Autowired
    CostService(CostDao costDao,
                CostKindDao costKindDao,
                AllocatedCostDefinitionDao allocatedCostDefinitionDao){

        checkNotNull(allocatedCostDefinitionDao, "allocatedCostDefinitionDao must not be null");
        checkNotNull(costDao, "costDao must not be null");
        checkNotNull(costKindDao, "costKindDao must not be null");

        this.allocatedCostDefinitionDao = allocatedCostDefinitionDao;
        this.costKindDao = costKindDao;
        this.costDao = costDao;
    }


    public Set<EntityCost> findByEntityReference(EntityReference ref){
        return costDao.findByEntityReference(ref);
    }


    public Set<EntityCost> findBySelector(IdSelectionOptions selectionOptions,
                                          EntityKind targetKind,
                                          int year){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return costDao.findBySelector(genericSelector, year);
    }


    public EntityCostsSummary summariseByCostKindAndSelector(Long costKindId,
                                                             IdSelectionOptions selectionOptions,
                                                             EntityKind targetKind,
                                                             int year,
                                                             int limit){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        Set<EntityCost> topCosts = costDao
                .findTopCostsForCostKindAndSelector(
                        costKindId,
                        year,
                        genericSelector,
                        limit);


        BigDecimal totalCost = costDao
                .getTotalForKindAndYearBySelector(
                        costKindId,
                        year,
                        genericSelector);

        Tuple2<Integer, Integer> mappedAndMissingCounts = costDao
                .getMappedAndMissingCountsForKindAndYearBySelector(
                        costKindId,
                        year,
                        genericSelector);

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

    public void populateAllocatedCosts() {

        Set<AllocatedCostDefinition> allocatedCostDefinitions = allocatedCostDefinitionDao.findAll();
        Map<Long, CostKindWithYears> costKindsById = indexBy(costKindDao.findAll(), d -> d.costKind().id().get());

        allocatedCostDefinitions
                .forEach(defn -> {

                    CostKindWithYears sourceKind = costKindsById.get(defn.sourceCostKind().id());
                    CostKindWithYears targetKind = costKindsById.get(defn.targetCostKind().id());

                    Integer maxYear = sourceKind.years()
                            .stream()
                            .max(comparingInt(k -> k))
                            .orElse(DateTimeUtilities.today().getYear()); // Take the most recent year costs are for

                    LOG.info(format("Allocating %s costs on %s to %s costs on %s using allocation scheme %s",
                            defn.sourceCostKind().name().orElse("Unknown"),
                            sourceKind.costKind().subjectKind(),
                            defn.targetCostKind().name().orElse("Unknown"),
                            targetKind.costKind().subjectKind(),
                            defn.allocationScheme().name().orElse("Unknown")));

                    allocateCostsByDefinition(defn, maxYear);

                });
    }

    public void allocateCostsByDefinition(AllocatedCostDefinition defn, Integer year) {
        allocatedCostDefinitionDao.allocateCostsByDefinition(defn, year);
    }

}
