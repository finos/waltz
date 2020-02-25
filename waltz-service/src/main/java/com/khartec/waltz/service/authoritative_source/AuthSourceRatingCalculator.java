/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.entity_hierarchy.EntityHierarchyDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromCollection;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.stream.Collectors.toList;


@Service
public class AuthSourceRatingCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(AuthSourceRatingCalculator.class);

    private final ApplicationIdSelectorFactory appIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeDao dataTypeDao;
    private final EntityHierarchyDao entityHierarchyDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;


    @Autowired
    public AuthSourceRatingCalculator(DataTypeDao dataTypeDao,
                                      EntityHierarchyDao entityHierarchyDao,
                                      LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                      LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(entityHierarchyDao, "entityHierarchyDao cannot be null");
        checkNotNull(ratingsCalculator, "ratingsCalculator cannot be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");

        this.dataTypeDao = dataTypeDao;
        this.entityHierarchyDao = entityHierarchyDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
        this.ratingsCalculator = ratingsCalculator;
    }


    // use dataTypeId variant, want to move away from codes
    @Deprecated
    public int[] update(String dataTypeCode, EntityReference vantageRef) {
        DataType dataType = dataTypeDao.getByCode(dataTypeCode);
        if (dataType == null) {
            LOG.error("Cannot update ratings for data type code: {} for vantage point: {} as cannot find corresponding data type",
                    dataTypeCode,
                    vantageRef);
            return new int[0];
        }
        return update(dataType, vantageRef);
    }


    public int[] update(long dataTypeId, EntityReference vantageRef) {
        DataType dataType = dataTypeDao.getById(dataTypeId);
        if (dataType == null) {
            LOG.error("Cannot update ratings for data type id: {} for vantage point: {} as cannot find corresponding data type",
                    dataTypeId,
                    vantageRef);
            return new int[0];
        }
        return update(dataType, vantageRef);
    }


    private int[] update(DataType dataType, EntityReference vantageRef) {
        LOG.debug("Updating ratings for auth source - dataType name: {}, id: {}, vantage point: {}",
                dataType.name(),
                dataType.id().get(),
                vantageRef);

        IdSelectionOptions selectorOptions = mkOpts(vantageRef, HierarchyQueryScope.CHILDREN);
        Select<Record1<Long>> selector = appIdSelectorFactory.apply(selectorOptions);
        Set<Long> dataTypeDescendents = entityHierarchyDao
                .findDesendents(dataType.entityReference())
                .stream()
                .map(d -> d.id().get())
                .collect(Collectors.toSet());

        Collection<LogicalFlowDecorator> impactedDecorators = logicalFlowDecoratorDao
                .findByEntityIdSelectorAndKind(
                        EntityKind.APPLICATION,
                        selector,
                        EntityKind.DATA_TYPE)
                .stream()
                .filter(decorator -> dataTypeDescendents.contains(decorator.decoratorEntity().id()))
                .collect(toList());

        Collection<LogicalFlowDecorator> reRatedDecorators = ratingsCalculator.calculate(impactedDecorators);

        Set<LogicalFlowDecorator> modifiedDecorators = SetUtilities.minus(
                fromCollection(reRatedDecorators),
                fromCollection(impactedDecorators));

        LOG.debug("Need to update {} ratings due to auth source change - dataType name: {}, id: {}, parent: {}",
                modifiedDecorators.size(),
                dataType.name(),
                dataType.id().get(),
                vantageRef);

        return updateDecorators(modifiedDecorators);
    }


    private int[] updateDecorators(Set<LogicalFlowDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");
        if (decorators.isEmpty()) return new int[] {};
        return logicalFlowDecoratorDao.updateDecorators(decorators);
    }

}
