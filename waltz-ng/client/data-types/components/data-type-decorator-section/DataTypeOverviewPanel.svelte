<script>

    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import _ from "lodash";
    import DataTypeDecoratorTreeView from "../../../common/svelte/DataTypeDecoratorTreeView.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";
    import {logicalFlowStore} from "../../../svelte-stores/logical-flow-store";
    import {enrichedDecorators, selectedDecorator, selectedDataType} from "./data-type-decorator-section-store"


    export let primaryEntityReference;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    const root = {name: "Root"};

    let activeMode = Modes.VIEW;
    let selectionOptions;
    let relatedDataTypesCall;
    let permissionsCall;
    let flowCall;
    let ratingCharacteristicsCall;
    let usageCharacteristicsCall;

    let workingDataTypes = [];
    let addedDataTypeIds = [];
    let removedDataTypeIds = [];

    function onSelect(evt) {
        const dataType = evt.detail;
        const decorator = _.get(decoratorsByDataTypeId, dataType.id);
        $selectedDecorator = decorator;
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

        const cmd = {
            entityReference: primaryEntityReference,
            addedDataTypeIds,
            removedDataTypeIds,
        };

        dataTypeDecoratorStore.save(primaryEntityReference, cmd)
            .then(() => {
                toasts.success("Successfully saved data types");
                relatedDataTypesCall = dataTypeDecoratorStore.findBySelector(primaryEntityReference.kind, selectionOptions, true);
                activeMode = Modes.VIEW;
            })
            .catch(e => displayError("Could not save data type changes", e));
    }

    $: {
        if (primaryEntityReference) {
            selectionOptions = mkSelectionOptions(primaryEntityReference);
            relatedDataTypesCall = dataTypeDecoratorStore.findBySelector(primaryEntityReference.kind, selectionOptions);
            permissionsCall = logicalFlowStore.findPermissionsForFlow(primaryEntityReference?.id);
            flowCall = logicalFlowStore.getById(primaryEntityReference.id);
        }
    }

    $: {
        if (logicalFlow){

            console.log({logicalFlow});

            const cmd = {
                source: logicalFlow.source,
                target: logicalFlow.target
            }

            console.log({cmd})
            usageCharacteristicsCall = dataTypeDecoratorStore.findDatatypeUsageCharacteristics(logicalFlow);
            ratingCharacteristicsCall = dataTypeDecoratorStore.findDataTypeRatingCharacteristics(cmd);
        }
    }

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

    $: console.log({ratingCharacteristics, usageCharacteristics});

</script>


<div class="row">
    {#if activeMode === Modes.VIEW}
        <div class="col-sm-12">
            <DataTypeDecoratorTreeView decorators={dataTypeDecorators}
                                       on:select={onSelect}
                                       nonConcreteSelectable={true}
                                       expanded={true}/>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
                        title={!hasEditPermission ? "You do not have permission to edit logical flows and associated data types" : ""}
                        disabled={!hasEditPermission}
                        on:click={edit}>
                    <Icon name="pencil"/>Edit
                </button>
            </div>
        </div>
    {:else if activeMode === Modes.EDIT}
        <div class="col-sm-12">
            <DataTypeTreeSelector multiSelect={true}
                                  expanded={true}
                                  nonConcreteSelectable={false}
                                  selectionFilter={selectionFilter}
                                  on:select={toggleDataType}
                                  {ratingCharacteristics}
                                  {usageCharacteristics}/>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
                        title={_.isEmpty(workingDataTypes) ? "At least one data type must be associated to this flow" : ""}
                        disabled={_.isEmpty(workingDataTypes)}
                        on:click={save}>
                    <Icon name="floppy-o"/>Save
                </button>
                |
                <button class="btn btn-skinny"
                        on:click={cancelEdit}>
                    <Icon name="ban"/>Cancel
                </button>
            </div>
        </div>
    {/if}
</div>
