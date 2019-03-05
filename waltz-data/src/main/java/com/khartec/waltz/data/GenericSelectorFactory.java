package com.khartec.waltz.data;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class GenericSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory;
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory;

    @Autowired
    public GenericSelectorFactory(ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                  ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory,
                                  DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                  FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory,
                                  LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory,
                                  MeasurableIdSelectorFactory measurableIdSelectorFactory,
                                  OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory) {

        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.changeInitiativeIdSelectorFactory = changeInitiativeIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.flowDiagramIdSelectorFactory = flowDiagramIdSelectorFactory;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
        this.organisationalUnitIdSelectorFactory = organisationalUnitIdSelectorFactory;
    }


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
                ApplicationIdSelectionOptions appSelectionOptions = ApplicationIdSelectionOptions.mkOpts(selectionOptions);
                return applicationIdSelectorFactory.apply(appSelectionOptions);
            case CHANGE_INITIATIVE:
                return changeInitiativeIdSelectorFactory.apply(selectionOptions);
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