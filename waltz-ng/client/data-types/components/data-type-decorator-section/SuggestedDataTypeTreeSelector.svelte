<script>

    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import _ from "lodash";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";

    export let logicalFlow;
    export let selectionFilter;
    export let onSelect;
    export let ratingCharacteristics;
    export let usageCharacteristics;

    let suggestedDataTypesCall;
    let showSuggested = true;
    let suggestedDataTypes = [];

    $: {
        if (!_.isEmpty(logicalFlow)){
            suggestedDataTypesCall = dataTypeStore.findSuggestedByRef(logicalFlow);
        }
    }

    $: suggestedDataTypes = _.map($suggestedDataTypesCall?.data, d => d.id);

</script>


{#if showSuggested}
    <DataTypeTreeSelector multiSelect={true}
                          expanded={true}
                          nonConcreteSelectable={false}
                          {selectionFilter}
                          on:select={onSelect}
                          dataTypeIds={suggestedDataTypes}
                          {ratingCharacteristics}
                          {usageCharacteristics}/>
    <div class="help-block">
        This is a filtered list of data types showing only those currently related to the upstream entity, alternatively you can
        <strong>
            <button class="btn btn-skinny"
                    on:click={() => showSuggested = false}>
                show all data types
            </button>
        </strong>
    </div>
{:else }
    <DataTypeTreeSelector multiSelect={true}
                          expanded={true}
                          nonConcreteSelectable={false}
                          {selectionFilter}
                          on:select={onSelect}
                          {ratingCharacteristics}
                          {usageCharacteristics}/>
    <div class="help-block">
        Currently showing all data types, for a filtered view
        <strong>
            <button class="btn btn-skinny"
                    on:click={() => showSuggested = true}>
                show selected data types only
            </button>
        </strong>
    </div>
{/if}
