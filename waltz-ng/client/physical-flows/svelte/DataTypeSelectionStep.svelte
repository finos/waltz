<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {dataTypes, expandedSections, physicalSpecification, skipDataTypes} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
    import {determineExpandedSections, sections} from "../../data-flow/components/svelte/propose-data-flow/propose-data-flow-utils";
    import Icon from "../../common/svelte/Icon.svelte";
    import DataTypeTreeSelector from "../../common/svelte/DataTypeTreeSelector.svelte";
    import {dataTypeStore} from "../../svelte-stores/data-type-store";
    import {onMount} from "svelte";
    import NoData from "../../common/svelte/NoData.svelte";

    let dataTypeList = [];

    onMount(() => {
        $dataTypes = [];
        dataTypeList = [];
    })

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.DATA_TYPE);
    }

    function onSelectDataType(d) {
        const alreadySelected = _.find(dataTypeList, dt => dt === d.detail.id);
        if (alreadySelected) {
            dataTypeList = _.without(dataTypeList, d.detail.id)
        } else {
            dataTypeList = _.concat(dataTypeList, d.detail.id);
        }
    }

    function editDataTypes() {
        $skipDataTypes = false;
        $dataTypes = [];
    }

    function updateDataTypes() {
        $dataTypes = dataTypeList;
    }

    $: dataTypeCall = dataTypeStore.findAll();
    $: dataTypesById = _.keyBy($dataTypeCall?.data, d => d.id);

    $: selectionFilter = (dataType) => {
        return !_.includes(dataTypeList, dataType.id);
    }

    $: expanded = _.includes($expandedSections, sections.DATA_TYPE);

    $: done = dataTypeList.length>0

    function skip() {
        $skipDataTypes = true;
    }

</script>

<StepHeader label="Data Types"
            icon="file-code-o"
            checked={!_.isEmpty($dataTypes)}
            {expanded}
            onToggleExpanded={toggleSection}>
</StepHeader>

{#if $physicalSpecification?.id}
    <NoData type="warning">
        <Icon name="exclamation-triangle"/>
        Adding data types to an existing specification will add them to all flows sharing that specification
    </NoData>
{/if}

{#if expanded}
    <div class="step-body">

        {#if !_.isEmpty($dataTypes) || $skipDataTypes}
            <div>
                <span style="font-weight: lighter">Selected Data Types:</span>
                {#if _.isEmpty($dataTypes)}
                    <span style="font-style: italic">No data types selected</span>
                {:else}
                    <ul>
                        {#each $dataTypes as dataTypeId}
                            <li>
                                {_.get(dataTypesById, [dataTypeId, "name"], "?")}
                            </li>
                        {/each}
                    </ul>
                {/if}
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => editDataTypes()}>
                <Icon name="times"/>
                Pick different data types
            </button>

        {:else}

            <div class="help-block">
                <Icon name="info-circle"/>
                Select data types from the tree below. These will be added to the physical specification and the logical
                flow.
            </div>

            <DataTypeTreeSelector multiSelect={true}
                                  on:select={onSelectDataType}
                                  nonConcreteSelectable={false}
                                  {selectionFilter}/>

            <br>
            <button class="btn btn-skinny"
                    disabled={!done}
                    on:click={() => updateDataTypes()}>
                Done
            </button>
            <button class="btn btn-skinny"
                    on:click={() => skip()}>
                Skip
            </button>

        {/if}
    </div>
{/if}

<style type="text/scss">
    @import "../../../style/_variables.scss";

    .step-body {
        padding-left: 1em;
    }

</style>