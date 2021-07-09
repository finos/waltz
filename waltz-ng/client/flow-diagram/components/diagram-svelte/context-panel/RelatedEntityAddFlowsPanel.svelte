<script>
    import {createEventDispatcher} from "svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import model from "../store/model";
    import {toGraphFlow, toGraphNode} from "../../../flow-diagram-utils";
    import _ from "lodash";
    import {positions} from "../store/layout";
    import {changeInitiativeStore} from "../../../../svelte-stores/change-initiative-store";
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import {mkRef} from "../../../../common/entity-utils";


    export let entity;
    export let canEdit;
    const dispatch = createEventDispatcher();


    $: entityCall = dataTypeStore.getById(entity.data.id);
    $: entityData = $entityCall.data;

    const Modes = {
        VIEW: "VIEW",
        CONFIRM_ADD: "CONFIRM_ADD"
    };

    let activeMode = Modes.VIEW;

    function cancel() {
        dispatch("cancel");
    }

    function remove() {
        dispatch("remove");
        cancel();
    }

    $: opts = mkSelectionOptions(entity.data);

    $: flowsCall = opts && logicalFlowStore.findBySelector(opts);
    $: relatedFlows = $flowsCall.data;

    $: existingFlowIds = _
        .chain($model.flows)
        .filter(n => n.data.kind === 'LOGICAL_DATA_FLOW')
        .map(n => n.data.id)
        .value();

    $: nodeIds = _.chain($model.nodes)
        .filter(n => n.data.kind === 'APPLICATION')
        .map(n => n.data.id)
        .value();

    let toAdd = [];

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



</script>

<div>
    <strong>
        <EntityLink ref={entity.data}/>
    </strong>
    <span class="text-muted small">( {entityData.code} )</span>
</div>
{#if activeMode === Modes.VIEW}
    <div class="help-block">
        {entityData.description}
    </div>
    {#if canEdit}
        <ul>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => addRelatedFlows()}>
                    <Icon name="plus"/>
                    Add related flows
                </button>
            </li>
            <li>
                <button class="btn btn-skinny"
                        on:click={remove}>
                    <Icon name="trash"/>
                    Remove
                </button>
            </li>
        </ul>
    {/if}
    <div class="context-panel-footer">
        <button class="btn btn-skinny"
                on:click={cancel}>
            <Icon name="fw"/>
            Cancel
        </button>
    </div>
    {:else if activeMode = Modes.CONFIRM_ADD}
    <div>Are you sure you want to add {_.size(toAdd)} new flows?</div>
    <button class="btn btn-success"
            on:click={saveNewFlows}>
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

    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>