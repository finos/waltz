/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.fromCollection;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.stream.Collectors.toList;


@Service
public class AuthSourceRatingCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(AuthSourceRatingCalculator.class);

    private final DataTypeDao dataTypeDao;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;


    @Autowired
    public AuthSourceRatingCalculator(DataTypeDao dataTypeDao,
                                      ApplicationIdSelectorFactory appIdSelectorFactory,
                                      LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                      LogicalFlowDecoratorDao logicalFlowDecoratorDao) {

        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(ratingsCalculator, "ratingsCalculator cannot be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");

        this.dataTypeDao = dataTypeDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
        this.appIdSelectorFactory = appIdSelectorFactory;
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
        LOG.info("Updating ratings for auth source - dataType: {}, vantage point: {}",
                dataType,
                vantageRef);

        IdSelectionOptions selectorOptions = mkOpts(vantageRef, HierarchyQueryScope.CHILDREN);
        Select<Record1<Long>> selector = appIdSelectorFactory.apply(selectorOptions);

        Collection<LogicalFlowDecorator> impactedDecorators = logicalFlowDecoratorDao
                .findByEntityIdSelectorAndKind(
                        EntityKind.APPLICATION,
                        selector,
                        EntityKind.DATA_TYPE)
                .stream()
                .filter(decorator -> decorator.decoratorEntity().id() == dataType.id().get())
                .collect(toList());

        Collection<LogicalFlowDecorator> reRatedDecorators = ratingsCalculator.calculate(impactedDecorators);

        Set<LogicalFlowDecorator> modifiedDecorators = SetUtilities.minus(
                fromCollection(reRatedDecorators),
                fromCollection(impactedDecorators));

        LOG.info("Need to update {} ratings due to auth source change - dataType: {}, parent: {}",
                modifiedDecorators.size(),
                dataType,
                vantageRef);

        return updateDecorators(modifiedDecorators);
    }


    private int[] updateDecorators(Set<LogicalFlowDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");
        if (decorators.isEmpty()) return new int[] {};
        return logicalFlowDecoratorDao.updateDecorators(decorators);
    }

}
