<script>
    import {processor, selectedNode, store} from "../diagram-model-store";
    import _ from "lodash";
    import {mkFlows, prepareUpdateCommands} from "./flow-resolver";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {Directions} from "./panel-utils";
    import {createEventDispatcher} from "svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";

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

    $: existingFlows = _.map($store.model.flows, d => d.data);
    $: existingNodes = _.map($store.model.nodes, d => d.data);
    $: possibleFlows = mkFlows(logicalFlows, $selectedNode, direction === Directions.UPSTREAM, existingNodes);
    $: checkedFlows = _
        .chain(possibleFlows)
        .filter(d => d.used)
        .value();

    function updateFlows() {
        const updateCmds = prepareUpdateCommands(
            checkedFlows,
            existingNodes,
            direction === Directions.UPSTREAM,
            $selectedNode);

        $processor(updateCmds);
        cancel();
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
    <pre>{JSON.stringify(possibleFlows, "", null)}</pre>
    <div class="waltz-scroll-region-250">
        {#each possibleFlows as flow}
            <label>
                <input type="checkbox"
                       checked={_.includes(checkedFlows, flow)}
                       on:click={() => flow.used = !flow.used}>
                <EntityLabel ref={flow.counterpartEntity}/>
            </label>
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