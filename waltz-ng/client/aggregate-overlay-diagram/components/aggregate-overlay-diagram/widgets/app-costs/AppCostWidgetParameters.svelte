<script context="module">
    import {writable} from "svelte/store";

    const selectedCostKinds = writable([]);
    const selectedAllocationScheme = writable(null);

    export function resetParameters() {
        selectedCostKinds.set([]);
        selectedAllocationScheme.set(null);
    }

</script>

<script>
    import {getContext} from "svelte";
    import AllocationSchemePicker
        from "../../../../../common/svelte/entity-pickers/AllocationSchemePicker.svelte";
    import CostKindPicker from "../../../../../common/svelte/entity-pickers/CostKindPicker.svelte";
    import _ from "lodash";

    const selectedOverlay = getContext("selectedOverlay");
    const widgetParameters = getContext("widgetParameters");

    let selectedDefinition;

    const Modes = {
        ALLOCATION_SCHEME_PICKER: "ALLOCATION_SCHEME_PICKER",
        COST_KIND_PICKER: "COST_KIND_PICKER",
        SUMMARY: "SUMMARY"
    }

    let activeMode = $selectedAllocationScheme
        ? Modes.SUMMARY
        : Modes.ALLOCATION_SCHEME_PICKER;

    function onSelect() {
        $widgetParameters = {
            allocationSchemeId: $selectedAllocationScheme.id,
            costKindIds: selectedCostKindIds,
        };

        $selectedOverlay = null;

        activeMode = Modes.SUMMARY;
    }


    function onSelectAllocationScheme(scheme) {
        activeMode = Modes.COST_KIND_PICKER
        $selectedAllocationScheme = scheme;
    }

    function onSelectCostKind(costKind) {
        $selectedCostKinds = _.concat($selectedCostKinds, costKind)
    }

    function changeAllocationScheme() {
        activeMode = Modes.ALLOCATION_SCHEME_PICKER
        $selectedAllocationScheme = null;
        $selectedCostKinds = [];
    }

    $: selectedCostKindIds = _.map($selectedCostKinds, d => d.id);

    $: incompleteSelection = _.isEmpty($selectedCostKinds) || _.isNil($selectedAllocationScheme);

</script>


{#if activeMode === Modes.ALLOCATION_SCHEME_PICKER}
    <div class="help-block">
        Select an allocation scheme from the list below,
        then select one or more cost kinds to apply the allocations to.
    </div>
    <AllocationSchemePicker onSelect={onSelectAllocationScheme}/>
{:else if activeMode === Modes.COST_KIND_PICKER}
    <div class="help-block">
        Select an allocation scheme from the list below,
        then select one or more the cost kinds to apply the allocations to.
    </div>
    <div>Allocation Scheme: {$selectedAllocationScheme.name}</div>
    <CostKindPicker onSelect={onSelectCostKind}
                    selectionFilter={ck => !_.includes(selectedCostKindIds, ck.id)}/>
    {#if !_.isEmpty(selectedCostKindIds)}
        <div>
            Selected Cost Kinds:
            <ul>
                {#each $selectedCostKinds as kind}
                    <li>
                        {kind.name}
                    </li>
                {/each}
            </ul>
        </div>
    {/if}
    <span>
         <button class="btn btn-success"
                 on:click={onSelect}
                 disabled={incompleteSelection}>
            Load data
        </button>
        <button class="btn btn-skinny"
                on:click={changeAllocationScheme}>
            Change allocation scheme
        </button>
    </span>
{:else if activeMode === Modes.SUMMARY}
    <div>Allocation Scheme: {$selectedAllocationScheme.name}</div>
    <br>
    <div>
        Selected Cost Kinds:
        <ul>
            {#each $selectedCostKinds as kind}
                <li>
                    {kind.name}
                </li>
            {/each}
        </ul>
    </div>
    <button class="btn btn-skinny"
            on:click={changeAllocationScheme}>
        Change allocation scheme
    </button>
{/if}
