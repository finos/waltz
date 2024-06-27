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

package org.finos.waltz.data;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.attestation.AttestationIdSelectorFactory;
import org.finos.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import org.finos.waltz.data.change_unit.ChangeUnitIdSelectorFactory;
import org.finos.waltz.data.data_type.DataTypeIdSelectorFactory;
import org.finos.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import org.finos.waltz.data.licence.LicenceIdSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowDecoratorIdSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.data.person.PersonIdSelectorFactory;
import org.finos.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.common.Checks.checkNotNull;

public class GenericSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory = new ChangeInitiativeIdSelectorFactory();
    private final ChangeUnitIdSelectorFactory changeUnitIdSelectorFactory = new ChangeUnitIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory = new FlowDiagramIdSelectorFactory();
    private final LicenceIdSelectorFactory licenceIdSelectorFactory = new LicenceIdSelectorFactory();
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final LogicalFlowDecoratorIdSelectorFactory logicalFlowDecoratorIdSelectorFactory = new LogicalFlowDecoratorIdSelectorFactory();
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    private final AttestationIdSelectorFactory attestationIdSelectorFactory = new AttestationIdSelectorFactory();
    private final PersonIdSelectorFactory personIdSelectorFactory = new PersonIdSelectorFactory();
    private final PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory = new PhysicalSpecificationIdSelectorFactory();
    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


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
            case LICENCE:
                return licenceIdSelectorFactory.apply(selectionOptions);
            case LOGICAL_DATA_FLOW:
                return logicalFlowIdSelectorFactory.apply(selectionOptions);
            case LOGICAL_DATA_FLOW_DATA_TYPE_DECORATOR:
                return logicalFlowDecoratorIdSelectorFactory.apply(selectionOptions);
            case MEASURABLE:
                return measurableIdSelectorFactory.apply(selectionOptions);
            case ORG_UNIT:
                return organisationalUnitIdSelectorFactory.apply(selectionOptions);
            case ATTESTATION:
                return attestationIdSelectorFactory.apply(selectionOptions);
            case PHYSICAL_FLOW:
                return physicalFlowIdSelectorFactory.apply(selectionOptions);
            case PHYSICAL_SPECIFICATION:
                return specificationIdSelectorFactory.apply(selectionOptions);
            case PERSON:
                return personIdSelectorFactory.apply(selectionOptions);
            default:
                throw new UnsupportedOperationException(String.format("Cannot make generic selector for kind: %s", kind));
        }
    }
}