<script>
    import {createEventDispatcher} from "svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import model from "../store/model";
    import {toGraphId, toGraphNode} from "../../../flow-diagram-utils";
    import _ from "lodash";
    import {positions} from "../store/layout";
    import {changeInitiativeStore} from "../../../../svelte-stores/change-initiative-store";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {diagram} from "../store/diagram";
    import overlay from "../store/overlay";
    import {getNewOverlay} from "./group-utils";

    export let entity;
    export let canEdit;
    const dispatch = createEventDispatcher();

    const Modes = {
        VIEW: "VIEW",
        CONFIRM_ADD_APPS: "CONFIRM_ADD_APPS",
        ADD_TO_OVERLAY_GROUP: "ADD_TO_OVERLAY_GROUP"
    };

    let activeMode = Modes.VIEW;
    let toAdd = [];

    $: opts = mkSelectionOptions(entity.data);

    $: entityCall = entity.data.kind === 'CHANGE_INITIATIVE'
        ? changeInitiativeStore.getById(entity.data.id)
        : measurableStore.getById(entity.data.id);

    $: entityData = $entityCall.data;

    $: overlayGroupsCall = flowDiagramOverlayGroupStore.findByDiagramId($diagram.id);
    $: overlayGroups = $overlayGroupsCall.data;

    $: appsCall = opts && applicationStore.findBySelector(opts);
    $: relatedApps = $appsCall.data;

    $: nodeIds = _
        .chain($model.nodes)
        .filter(n => n.data.kind === 'APPLICATION')
        .map(n => n.data.id)
        .value();

    function cancel() {
        dispatch("cancel");
    }

    function remove() {
        dispatch("remove", entity);
        cancel();
    }

    function addRelatedApplications(){
        toAdd = _
            .chain(relatedApps)
            .filter(a => !_.includes(nodeIds, a.id))
            .map(n => toGraphNode(n))
            .value();

        activeMode = Modes.CONFIRM_ADD_APPS;
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

    function addToOverlayGroup(){
        activeMode = Modes.ADD_TO_OVERLAY_GROUP
    }

    function saveToGroup(group){
        const newOverlay = getNewOverlay(entity.data, $overlay.groupOverlays[group.id]);
        const overlayToAdd = Object.assign(
            {},
            newOverlay,
            {
                groupRef: toGraphId({kind:"GROUP", id: group.id}),
                applicationIds: _.map(relatedApps, d => d.id)
            });
        overlay.addOverlay(overlayToAdd);
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
            {#if entity.data.kind === 'MEASURABLE'}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => addToOverlayGroup()}>
                    <Icon name="plus"/>
                    Add to an overlay group
                </button>
            </li>
            {/if}
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
{:else if activeMode === Modes.CONFIRM_ADD_APPS}
    <div>Are you sure you want to add {_.size(toAdd)} new nodes?</div>
    {#if _.size(toAdd) > 100}
    <div class="alert alert-warning">
        <Icon name="warning"/>
        There are too many nodes to add at once.
        You may be able to choose a child of
        <strong>{entity.data.name}</strong>
        for a more selective group of applications.
    </div>
    {/if}
    <button class="btn btn-success"
            on:click={saveNewNodes}
            disabled={_.size(toAdd) > 100}>
        OK
    </button>
    <button class="btn btn-default"
            on:click={() => activeMode = Modes.VIEW}>
        Cancel
    </button>
{:else if activeMode === Modes.ADD_TO_OVERLAY_GROUP}
    {#if _.size(overlayGroups) === 0}
        You have no associated groups. Select the 'Overlays' tab to add one.
    {:else }
        <div>Add to one of the overlay groups below:</div>
        {#each overlayGroups as group}
            <table class="table table-condensed">
                <tbody>
                    <tr>
                        <td>{group.name}</td>
                        <td><button class="btn btn-skinny"
                                    on:click={() => saveToGroup(group)}>
                                <Icon name="plus"/>Add
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        {/each}
    {/if}
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