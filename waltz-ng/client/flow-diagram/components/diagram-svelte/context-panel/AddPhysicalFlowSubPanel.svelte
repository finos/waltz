<script>
    import _ from "lodash";
    import {preparePhysicalFlows, preparePhysicalFlowUpdates} from "./flow-resolver";
    import {createEventDispatcher} from "svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import {physicalFlowStore} from "../../../../svelte-stores/physical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {physicalSpecStore} from "../../../../svelte-stores/physical-spec-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import model from "../store/model";


    const Modes = {
        OVERVIEW: "OVERVIEW",
        NODE: "NODE",
        FLOW: "FLOW",
    };

    const dispatch = createEventDispatcher();

    export let selected;

    $: flowRef = Object.assign({}, selected, {kind: 'LOGICAL_DATA_FLOW'});
    $: flowSelectionOpts = mkSelectionOptions(flowRef, 'EXACT' );
    $: physicalFlowCall = physicalFlowStore.findBySelector(flowSelectionOpts);
    $: physicalFlows = $physicalFlowCall.data;

    $: physicalSpecCall = physicalSpecStore.findBySelector(flowSelectionOpts)
    $: physicalSpecs = _.map($physicalSpecCall.data, r => Object.assign({}, r, {kind: 'PHYSICAL_SPECIFICATION'}));
    $: existing = _.flatMap($model.decorations);
    $: existingIds = _.map(existing, d => d.id);
    $: preparedFlows = preparePhysicalFlows(physicalFlows, physicalSpecs, _.map(existing, d => d.data));

    let activeMode = Modes.OVERVIEW;
    let selectionOptions = [];

    function cancel() {
        dispatch("cancel");
    }

    function updateFlows(){
        const updates = preparePhysicalFlowUpdates(preparedFlows);
        updates.additions.forEach(d => model.addDecoration(d));
        updates.removals.forEach(d => model.removeDecoration(d));
        cancel();
    }

</script>

<div class="waltz-scroll-region-250">
    <ul>
        {#each preparedFlows as flow}
            <li>
            <label>
                <input type="checkbox"
                       checked={_.includes(existingIds, toGraphId(flow.physicalFlow))}
                       on:click={() => flow.used = !flow.used}>
                <EntityLabel ref={flow.specification}/>
            </label>
            </li>
        {/each}
    </ul>
</div>

<div class="context-panel-footer">
    <button class="btn btn-skinny"
            on:click={() => updateFlows()}>
        Update flows
    </button>
    |
    <button class="btn btn-skinny"
            on:click={() => cancel()}>
        Cancel
    </button>
</div>


<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>