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
        CONFIRM_REMOVE: "CONFIRM_REMOVE",
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

    function remove() {
        activeMode = Modes.CONFIRM_REMOVE;
    }

    function doRemove() {
        dispatch("remove", entity);
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
                dx: _.random(-20, 160),
                dy: _.random(50, 120)
            });
        });
        activeMode = Modes.VIEW;
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
        activeMode = Modes.VIEW;
    }
</script>

<p class="help-block">External Id: {entityData.externalId || "-"}</p>
<p class="help-block">{entityData.description}</p>

{#if activeMode === Modes.VIEW}
    {#if canEdit}
        <ul>
            <li>
                <button class="btn btn-skinny"
                        on:click={addRelatedApplications}>
                    <Icon name="desktop"/>
                    Add related applications
                </button>
            </li>
            {#if entity.data.kind === 'MEASURABLE'}
            <li>
                <button class="btn btn-skinny"
                        on:click={addToOverlayGroup}>
                    <Icon name="star-o"/>
                    Add to an overlay group
                </button>
            </li>
            {/if}
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
        Sure you want to remove this related entity ?
    </div>
    <button class="btn btn-danger"
            on:click={doRemove}>
        OK
    </button>
    <button class="btn btn-default"
            on:click={() => activeMode = Modes.VIEW}>
        Cancel
    </button>
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
        <table class="table table-condensed">
            {#each overlayGroups as group}
                <tr>
                    <td>{group.name}</td>
                    <td>
                        <button class="btn btn-skinny"
                                on:click={() => saveToGroup(group)}>
                            <Icon name="plus"/>
                            Add
                        </button>
                    </td>
                </tr>
            {/each}
        </table>
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
</style>