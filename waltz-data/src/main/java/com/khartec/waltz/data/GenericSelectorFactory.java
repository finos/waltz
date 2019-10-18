/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import com.khartec.waltz.data.change_unit.ChangeUnitIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;

public class GenericSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory = new ChangeInitiativeIdSelectorFactory();
    private final ChangeUnitIdSelectorFactory changeUnitIdSelectorFactory = new ChangeUnitIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory = new FlowDiagramIdSelectorFactory();
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();


    public GenericSelector apply(IdSelectionOptions selectionOptions) {
        EntityKind kind = selectionOptions.entityReference().kind();

        ImmutableGenericSelector.Builder builder = ImmutableGenericSelector.builder()
                .kind(kind);

        if (selectionOptions.scope() == HierarchyQueryScope.EXACT) {
            Select<Record1<Long>> select = DSL.select(DSL.val(selectionOptions.entityReference().id()));
            return builder
                    .selector(select)
                    .build();
        }

        Select<Record1<Long>> ids = applySelectorForKind(kind, selectionOptions);
        builder.selector(ids);

        return builder.build();
    }


    /***
     * This method evaluates a selector for the target kind which may not match the kind in selection options
     * Used when creating Survey or Attestation runs where the target kind may be different from the selection kind
     * @param targetKind
     * @param selectionOptions
     * @return
     */
    public GenericSelector applyForKind(EntityKind targetKind, IdSelectionOptions selectionOptions) {
        checkNotNull(targetKind, "targetKind cannot be null");

        ImmutableGenericSelector.Builder builder = ImmutableGenericSelector.builder()
                .kind(targetKind);

        Select<Record1<Long>> ids = applySelectorForKind(targetKind, selectionOptions);
        builder.selector(ids);

        return builder.build();
    }


    /***
     * Apply a selector using the appropriate factory as defined by kind
     * @param kind
     * @param selectionOptions
     * @return
     */
    private Select<Record1<Long>> applySelectorForKind(EntityKind kind, IdSelectionOptions selectionOptions) {
        switch (kind) {
            case APPLICATION:
                return applicationIdSelectorFactory.apply(selectionOptions);
            case CHANGE_INITIATIVE:
                return changeInitiativeIdSelectorFactory.apply(selectionOptions);
            case CHANGE_UNIT:
                return changeUnitIdSelectorFactory.apply(selectionOptions);
            case DATA_TYPE:
                return dataTypeIdSelectorFactory.apply(selectionOptions);
            case FLOW_DIAGRAM:
                return flowDiagramIdSelectorFactory.apply(selectionOptions);
            case LOGICAL_DATA_FLOW:
                return logicalFlowIdSelectorFactory.apply(selectionOptions);
            case MEASURABLE:
                return measurableIdSelectorFactory.apply(selectionOptions);
            case ORG_UNIT:
                return organisationalUnitIdSelectorFactory.apply(selectionOptions);
            //todo: (KS) Add support for Person
            default:
                throw new UnsupportedOperationException(String.format("Cannot make generic selector for kind: %s", kind));
        }
    }
}