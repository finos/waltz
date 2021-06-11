<script>
    import {selectedNode} from "../diagram-model-store";
    import _ from "lodash";
    import {mkFlows, prepareUpdateCommands} from "./flow-resolver";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {Directions} from "./panel-utils";
    import {createEventDispatcher} from "svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import model from "../store/model";
    import {positions} from "../store/layout";

    const Modes = {
        OVERVIEW: "OVERVIEW",
        NODE: "NODE",
        FLOW: "FLOW",
    };

    const dispatch = createEventDispatcher();

    $: logicalFlowCall = logicalFlowStore.findByEntityReference(selected);
    $: logicalFlows = $logicalFlowCall.data

    export let selected;
    export let direction;

    let activeMode = Modes.OVERVIEW;
    let selectionOptions = [];

    $: existingFlows = _.map($model.flows, d => d.data);
    $: existingNodes = _.map($model.nodes, d => d.data);
    $: possibleFlows = mkFlows(logicalFlows, $selectedNode, direction === Directions.UPSTREAM, existingNodes);

    $: checkedFlows = _.filter(possibleFlows, d => d.used);

    function updateFlows() {
        const updates = prepareUpdateCommands(
            possibleFlows,
            existingNodes,
            direction === Directions.UPSTREAM,
            $selectedNode);

        updates.nodeAdditions.forEach(model.addNode);
        updates.flowAdditions.forEach(model.addFlow);
        updates.flowRemovals.forEach(model.removeFlow);
        updates.moves.forEach(positions.move);
    }

    $: oppositeDirection = (direction === Directions.UPSTREAM)
        ? Directions.DOWNSTREAM
        : Directions.UPSTREAM

    function cancel() {
        dispatch("cancel");
    }

</script>

<div>
    <p>
        <strong>{selected.name}</strong> is the {oppositeDirection.toLowerCase()} node.
        Pick an application or actor to be the {direction.toLowerCase()} node.
    </p>
    <div class="waltz-scroll-region-250">
        {#each possibleFlows as flow}
            <div class="checkbox">
                <label>
                    <input type="checkbox"
                           checked={_.includes(checkedFlows, flow)}
                           on:click={() => flow.used = !flow.used}>
                    <EntityLabel ref={flow.counterpartEntity}/>
                </label>
            </div>
        {/each}
    </div>
</div>
<button class="btn btn-link"
        on:click={() => cancel()}>
    Cancel
</button>
<button class="btn btn-link"
        on:click={() => updateFlows()}>
    Update flows
</button>


<style>
</style>