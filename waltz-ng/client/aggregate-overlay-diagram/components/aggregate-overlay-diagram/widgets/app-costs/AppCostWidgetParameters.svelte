<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import AllocationSchemePicker
        from "../../../../../report-grid/components/svelte/pickers/AllocationSchemePicker.svelte";
    import CostKindPicker from "../../../../../report-grid/components/svelte/pickers/CostKindPicker.svelte";
    import _ from "lodash";
    import AppCostOverlayCell from "./AppCostOverlayCell.svelte";

    const selectedDiagram = getContext("selectedDiagram");
    const widget = getContext("widget");
    const selectedOverlay = getContext("selectedOverlay");
    const selectedCostKinds = getContext("selectedCostKinds");
    const selectedAllocationScheme = getContext("selectedAllocationScheme");
    const widgetParameters = getContext("widgetParameters");
    const remoteMethod = getContext("remoteMethod");
    const overlayDataCall = getContext("overlayDataCall");

    let selectedCostKindIds = [];
    let selectedDefinition;

    const Modes = {
        ALLOCATION_SCHEME_PICKER: "ALLOCATION_SCHEME_PICKER",
        COST_KIND_PICKER: "COST_KIND_PICKER",
        SUMMARY: "SUMMARY"
    }

    let activeMode = $selectedAllocationScheme
        ? Modes.SUMMARY
        : Modes.ALLOCATION_SCHEME_PICKER;


    function mkGlobalProps(data) {
        const maxCost = _
            .chain(data)
            .map(d => d.totalCost)
            .max()
            .value();
        return {maxCost};
    }

    function onSelect() {

        $remoteMethod = aggregateOverlayDiagramStore.findAppCostForDiagram;

        $widgetParameters = {
            allocationSchemeId: $selectedAllocationScheme.id,
            costKindIds: selectedCostKindIds,
        }

        $selectedOverlay = null;

        $widget = {
            overlay: AppCostOverlayCell,
            mkGlobalProps
        };

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


{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}