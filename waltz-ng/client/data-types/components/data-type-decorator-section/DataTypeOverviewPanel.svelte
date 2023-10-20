<script>

    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import _ from "lodash";
    import DataTypeTreeView from "../../../common/svelte/DataTypeTreeView.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";


    export let primaryEntityReference;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    const root = {name: "Root"};

    let activeMode = Modes.VIEW;
    let selectionOptions;
    let relatedDataTypesCall;
    let selectedDataType;

    let workingDataTypes = [];
    let  addedDataTypeIds = [];
    let removedDataTypeIds = [];

    $: {
        if (primaryEntityReference) {
            selectionOptions = mkSelectionOptions(primaryEntityReference);
            relatedDataTypesCall = dataTypeDecoratorStore.findBySelector(primaryEntityReference.kind, selectionOptions);
        }
    }

    $: dataTypeDecorators = $relatedDataTypesCall?.data || [];
    $: dataTypes = _.map(dataTypeDecorators, d => d.dataTypeId)

    $: descoratorsByDataTypeId = _.keyBy(dataTypeDecorators, d => d.dataTypeId);

    $:  selectionFilter = (d) => !_.includes(workingDataTypes, d.id);

    function onSelect(evt) {
        selectedDataType = evt.detail;
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
        }

        dataTypeDecoratorStore.save(primaryEntityReference, cmd)
            .then(() => {
                toasts.success("Successfully saved data types");
                relatedDataTypesCall = dataTypeDecoratorStore.findBySelector(primaryEntityReference.kind, selectionOptions, true);
                activeMode = Modes.VIEW;
            })
            .catch(e => displayError("Could not save data type changes", e));
    }

    $: addedDataTypeIds = _.without(workingDataTypes, ...dataTypes);
    $: removedDataTypeIds = _.without(dataTypes, ...workingDataTypes);


</script>


<h4>Overview</h4>

<div class="row">
    {#if activeMode === Modes.VIEW}
        <div class="col-sm-6">
            <DataTypeTreeView dataTypeIds={dataTypes}
                              on:select={onSelect}
                              expanded={true}/>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
                        on:click={edit}>
                    <Icon name="pencil"/>Edit
                </button>
            </div>
        </div>
        <div class="col-sm-6">

            <div class="waltz-sub-section">
                {#if selectedDataType}
                    <h4>{selectedDataType.name}</h4>
                    <div class="help-block">
                        {selectedDataType.description}
                    </div>
                {/if}
            </div>
        </div>
    {:else if activeMode === Modes.EDIT}
        <div class="col-sm-12">
            <DataTypeTreeSelector multiSelect={true}
                                  expanded={true}
                                  nonConcreteSelectable={false}
                                  selectionFilter={selectionFilter}
                                  on:select={toggleDataType}/>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
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
