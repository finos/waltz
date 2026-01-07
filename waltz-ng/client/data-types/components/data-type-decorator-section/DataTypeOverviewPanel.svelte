<script>

    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";
    import {logicalFlowStore} from "../../../svelte-stores/logical-flow-store";
    import {
        enrichedDecorators,
        selectedDataType,
        selectedDecorator,
        viewData
    } from "./data-type-decorator-section-store"
    import SavingPlaceholder from "../../../common/svelte/SavingPlaceholder.svelte";
    import SuggestedDataTypeTreeSelector from "./SuggestedDataTypeTreeSelector.svelte";
    import {settingsStore} from "../../../svelte-stores/settings-store";
    import {DATAFLOW_PROPOSAL_SETTING_NAME} from "../../../common/constants"
    import {isDataFlowProposalsEnabled} from "../../../common/utils/settings-util";


    export let primaryEntityReference;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }
    const root = {name: "Root"};

    let activeMode = Modes.VIEW;
    let selectionOptions;
    let permissionsCall;
    let flowCall;
    let viewCall;
    let ratingCharacteristicsCall;
    let usageCharacteristicsCall;
    let saving = false;

    let workingDataTypes = [];
    let addedDataTypeIds = [];
    let removedDataTypeIds = [];

    function onSelect(evt) {
        const dataType = evt.detail;
        $selectedDecorator = _.get(decoratorsByDataTypeId, dataType.id);
        $selectedDataType = dataType;
    }

    function cancelEdit() {
        workingDataTypes = dataTypes;
        activeMode = Modes.VIEW;
    }

    function edit() {
        workingDataTypes = dataTypes;
        activeMode = Modes.EDIT;
    }

    function toggleDataType(evt) {
        const dataType = evt.detail;
        if (_.includes(workingDataTypes, dataType.id)) {
            workingDataTypes = _.without(workingDataTypes, dataType.id);
        } else {
            workingDataTypes = _.concat(workingDataTypes, dataType.id);
        }
    }

    function save() {

        saving = true;

        const cmd = {
            entityReference: primaryEntityReference,
            addedDataTypeIds,
            removedDataTypeIds,
        };

        dataTypeDecoratorStore.save(primaryEntityReference, cmd)
            .then(() => {
                saving = false;
                toasts.success("Successfully saved data types");
                activeMode = Modes.VIEW;
                viewCall = dataTypeDecoratorStore.getViewForParentRef(primaryEntityReference, true);
            })
            .catch(e => displayError("Could not save data type changes", e));
    }

    $: {
        if (primaryEntityReference) {
            selectionOptions = mkSelectionOptions(primaryEntityReference);
            permissionsCall = logicalFlowStore.findPermissionsForFlow(primaryEntityReference?.id);
            flowCall = logicalFlowStore.getById(primaryEntityReference.id);
            viewCall = dataTypeDecoratorStore.getViewForParentRef(primaryEntityReference);
        }
    }


    $: {
        if (!_.isEmpty(logicalFlow)){

            const cmd = {
                source: logicalFlow.source,
                target: logicalFlow.target
            }

            usageCharacteristicsCall = dataTypeDecoratorStore.findDatatypeUsageCharacteristics(logicalFlow);
            ratingCharacteristicsCall = dataTypeDecoratorStore.findDataTypeRatingCharacteristics(cmd);
        }
    }

    $: $viewData = $viewCall?.data;
    $: logicalFlow = $flowCall?.data;
    $: dataTypeDecorators = $enrichedDecorators || [];
    $: dataTypes = _.map(dataTypeDecorators, d => d.dataTypeId);
    $: ratingCharacteristics = $ratingCharacteristicsCall?.data;
    $: usageCharacteristics = $usageCharacteristicsCall?.data;

    $: decoratorsByDataTypeId = _.keyBy(dataTypeDecorators, d => d.dataTypeId);

    $: selectionFilter = (d) => !_.includes(workingDataTypes, d.id);

    $: addedDataTypeIds = _.without(workingDataTypes, ...dataTypes);
    $: removedDataTypeIds = _.without(dataTypes, ...workingDataTypes);

    $: permissions = $permissionsCall?.data || [];
    $: hasEditPermission = _.some(permissions, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));

    let settingsCall=settingsStore.loadAll();

    $: dataFlowProposalSetting = $settingsCall.data
        .filter(t => t.name === DATAFLOW_PROPOSAL_SETTING_NAME)
        [0];
    $: dataFlowProposalsEnabled = isDataFlowProposalsEnabled($settingsCall.data);

</script>


<div class="row">
    {#if activeMode === Modes.VIEW}
        <div class="col-sm-12">
            <DataTypeTreeSelector multiSelect={false}
                                  expanded={true}
                                  dataTypeIds={dataTypes}
                                  nonConcreteSelectable={false}
                                  selectionFilter={selectionFilter}
                                  on:select={onSelect}
                                  {ratingCharacteristics}
                                  {usageCharacteristics}/>
            {#if dataFlowProposalsEnabled!==undefined && !dataFlowProposalsEnabled}
                <div style="padding-top: 1em">
                    <button class="btn btn-skinny"
                            title={!hasEditPermission ? "You do not have permission to edit logical flows and associated data types" : ""}
                            disabled={!hasEditPermission}
                            on:click={edit}>
                        <Icon name="pencil"/>Edit
                    </button>
                </div>
            {/if}
        </div>
    {:else if activeMode === Modes.EDIT}
        <div class="col-sm-12">
            <SuggestedDataTypeTreeSelector {logicalFlow}
                                           selectionFilter={selectionFilter}
                                           onSelect={toggleDataType}
                                           {ratingCharacteristics}
                                           {usageCharacteristics}/>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
                        title={_.isEmpty(workingDataTypes) ? "At least one data type must be associated to this flow" : ""}
                        disabled={_.isEmpty(workingDataTypes) || saving}
                        on:click={save}>
                    <Icon name="floppy-o"/>Save
                </button>
                |
                <button class="btn btn-skinny"
                        on:click={cancelEdit}>
                    <Icon name="ban"/>Cancel
                </button>
                {#if saving}
                    <span>
                        <SavingPlaceholder/>
                    </span>
                {/if}
            </div>
        </div>
    {/if}
</div>
