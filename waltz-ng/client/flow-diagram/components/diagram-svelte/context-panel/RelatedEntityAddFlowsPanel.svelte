<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import model from "../store/model";
    import {toGraphFlow} from "../../../flow-diagram-utils";
    import _ from "lodash";
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";

    export let entity;
    export let canEdit;
    const dispatch = createEventDispatcher();

    const Modes = {
        VIEW: "VIEW",
        CONFIRM_ADD: "CONFIRM_ADD",
        CONFIRM_REMOVE: "CONFIRM_REMOVE"
    };

    function cancel() {
        dispatch("cancel");
    }

    function remove() {
        activeMode = Modes.CONFIRM_REMOVE;
    }

    function doRemove() {
        dispatch("remove", entity);
        cancel();
    }

    function addRelatedFlows(){
        toAdd = _
            .chain(relatedFlows)
            .filter(flow => !_.includes(existingFlowIds, flow.id))
            .filter(flow => _.includes(nodeIds, flow.source.id) &&  _.includes(nodeIds, flow.target.id))
            .map(n => toGraphFlow(n))
            .value();

        activeMode = Modes.CONFIRM_ADD;
    }

    function saveNewFlows(){
        toAdd.forEach(n => model.addFlow(n));
        cancel();
    }

    let toAdd = [];
    let activeMode = Modes.VIEW;

    $: entityCall = dataTypeStore.getById(entity.data.id);
    $: entityData = $entityCall.data;

    $: opts = mkSelectionOptions(entity.data);

    $: flowsCall = opts && logicalFlowStore.findBySelector(opts);
    $: relatedFlows = $flowsCall.data;

    $: existingFlowIds = _
        .chain($model.flows)
        .filter(n => n.data.kind === 'LOGICAL_DATA_FLOW')
        .map(n => n.data.id)
        .value();

    $: nodeIds = _
        .chain($model.nodes)
        .filter(n => n.data.kind === 'APPLICATION')
        .map(n => n.data.id)
        .value();
</script>


<p class="help-block">External Id: {entityData.code}</p>
<p class="help-block">{entityData.description}</p>

{#if activeMode === Modes.VIEW}
    {#if canEdit}
        <ul>
            <li>
                <button class="btn btn-skinny"
                        on:click={addRelatedFlows}>
                    <Icon name="random"/>
                    Add related flows
                </button>
            </li>
            <li style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em;">
                <button class="btn btn-skinny"
                        on:click={remove}>
                    <Icon name="trash"/>
                    Remove
                </button>
            </li>
        </ul>
    {/if}
{:else if activeMode === Modes.CONFIRM_REMOVE}
    <div>
        Sure you want to remove this datatype ?
    </div>
    <button class="btn btn-danger"
            on:click={doRemove}>
        OK
    </button>
    <button class="btn btn-default"
            on:click={() => activeMode = Modes.VIEW}>
        Cancel
    </button>
{:else if activeMode = Modes.CONFIRM_ADD}
    <div>
        Are you sure you want to add {_.size(toAdd)} new flows ?
    </div>
    {#if _.size(toAdd) > 100}
        <div class="alert alert-warning">
            <Icon name="warning"/>
            There are too many flows to add at once.
            You may be able to choose a child of
            <strong>{entity.data.name}</strong>
            for a more selective group of applications.
        </div>
    {/if}
    <button class="btn btn-success"
            on:click={saveNewFlows}
            disabled={_.size(toAdd) > 100}>
        OK
    </button>
    <button class="btn btn-default"
            on:click={() => activeMode = Modes.VIEW}>
        Cancel
    </button>
{/if}

<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }

</style>