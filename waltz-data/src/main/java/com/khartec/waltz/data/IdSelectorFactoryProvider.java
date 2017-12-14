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

package com.khartec.waltz.data;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class IdSelectorFactoryProvider {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory;


    @Autowired
    public IdSelectorFactoryProvider(ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                     ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory,
                                     DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                     LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory,
                                     MeasurableIdSelectorFactory measurableIdSelectorFactory,
                                     OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory) {
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(changeInitiativeIdSelectorFactory, "changeInitiativeIdSelectorFactory cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");
        checkNotNull(measurableIdSelectorFactory, "measurableIdSelectorFactory cannot be null");
        checkNotNull(organisationalUnitIdSelectorFactory, "organisationalUnitIdSelectorFactory cannot be null");

        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.changeInitiativeIdSelectorFactory = changeInitiativeIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
        this.organisationalUnitIdSelectorFactory = organisationalUnitIdSelectorFactory;
    }


    public IdSelectorFactory getForKind(EntityKind kind) {
        checkNotNull(kind, "kind cannot be null");

        switch (kind) {
            case APPLICATION:
                return applicationIdSelectorFactory;
            case CHANGE_INITIATIVE:
                return changeInitiativeIdSelectorFactory;
            case DATA_TYPE:
                return dataTypeIdSelectorFactory;
            case LOGICAL_DATA_FLOW:
                return logicalFlowIdSelectorFactory;
            case MEASURABLE:
                return measurableIdSelectorFactory;
            case ORG_UNIT:
                return organisationalUnitIdSelectorFactory;
            default:
                throw new IllegalArgumentException("No Id selector factory defined for " + kind);

        }
    }
}
