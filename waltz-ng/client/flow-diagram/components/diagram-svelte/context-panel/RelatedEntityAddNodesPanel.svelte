<script>
    import {createEventDispatcher} from "svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import model from "../store/model";
    import {toGraphNode} from "../../../flow-diagram-utils";
    import _ from "lodash";
    import {positions} from "../store/layout";
    import {changeInitiativeStore} from "../../../../svelte-stores/change-initiative-store";


    export let entity;
    export let canEdit;
    const dispatch = createEventDispatcher();


    $: entityCall = entity.data.kind === 'CHANGE_INITIATIVE'
        ? changeInitiativeStore.getById(entity.data.id)
        : measurableStore.getById(entity.data.id);
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
        dispatch("remove", entity);
        cancel();
    }

    $: opts = mkSelectionOptions(entity.data);

    $: appsCall = opts && applicationStore.findBySelector(opts);
    $: relatedApps = $appsCall.data;

    $: nodeIds = _
        .chain($model.nodes)
        .filter(n => n.data.kind === 'APPLICATION')
        .map(n => n.data.id)
        .value();

    let toAdd = [];

    function addRelatedApplications(){
        toAdd = _
            .chain(relatedApps)
            .filter(a => !_.includes(nodeIds, a.id))
            .map(n => toGraphNode(n))
            .value();

        activeMode = Modes.CONFIRM_ADD;
    }

    function saveNewNodes(){
        toAdd.forEach(n => {
            model.addNode(n)
            positions.move({
                id: n.id,
                dx: _.random(-80, 80),
                dy: _.random(50, 80)
            });
        });
        cancel();
    }



</script>

<div>
    <strong>
        <EntityLink ref={entity.data}/>
    </strong>
    <span class="text-muted small">( {entityData.externalId} )</span>
</div>
{#if activeMode === Modes.VIEW}
    {#if entity.data.kind === 'CHANGE_INITIATIVE'}
        <div class="help-block">
            <p>{entityData.changeInitiativeKind || "Unknown kind"}</p>
            {entityData.description}
        </div>
    {:else if entity.data.kind === 'MEASURABLE'}
        <div class="help-block">
            <p>{entity.category?.name || "Unknown category"}</p>
            {entityData.description}
        </div>
    {/if}
    {#if canEdit}
        <ul>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => addRelatedApplications()}>
                    <Icon name="plus"/>
                    Add related applications
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
    <div>Are you sure you want to add {_.size(toAdd)} new nodes?</div>
    <button class="btn btn-success"
            on:click={saveNewNodes}>
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