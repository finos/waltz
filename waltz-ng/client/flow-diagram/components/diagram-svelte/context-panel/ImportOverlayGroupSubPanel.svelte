<script>
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {createEventDispatcher} from "svelte";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import _ from "lodash";
    import overlay from "../store/overlay";
    import {refToString} from "../../../../common/entity-utils";

    let sourceDiagram;

    let dispatch = createEventDispatcher();

    function cancel() {
        dispatch("cancel")
        sourceDiagram = null;
    }

    $: groupsCall = sourceDiagram && flowDiagramOverlayGroupStore.findByDiagramId(sourceDiagram?.id);
    $: entriesCall = sourceDiagram && flowDiagramOverlayGroupStore.findOverlaysByDiagramId(sourceDiagram?.id);

    $: groups = $groupsCall?.data;

    $: currentOverlayEntityRefs = _.map(
        $overlay.groupOverlays[$overlay.selectedGroup?.id],
        d => refToString(d.data.entityReference));

    $: entriesByGroupId = _
            .chain($entriesCall?.data)
            .filter(d => !_.includes(currentOverlayEntityRefs, refToString(d.entityReference)))
            .groupBy("overlayGroupId")
            .value();

    function doImport(group){
        const entriesToAdd = _
            .chain(entriesByGroupId[group.id])
            .map(d => Object.assign({}, d, {groupRef: $overlay.selectedGroup.id}))
            .forEach(d => overlay.addOverlay(d))
            .value();

        cancel();
    }
</script>

{#if _.isNil(sourceDiagram)}
    <strong>Select a diagram to show a list of it's associated groups:</strong>
    <EntitySearchSelector on:select={e => sourceDiagram = e.detail}
                          placeholder="Search for diagram"
                          entityKinds={['FLOW_DIAGRAM']}>
    </EntitySearchSelector>
{:else}
    <strong>Select a group to import from {sourceDiagram.name}</strong>
    {#if groups}
    <ul>
        {#each groups as group}
            <li>
                <button class="btn btn-skinny"
                        disabled={_.isEmpty(entriesByGroupId[group.id])}
                        on:click={() => doImport(group)}>
                    {group.name}  ({_.size(entriesByGroupId[group.id])} - new entries)
                </button>
            </li>
        {:else}
            <li>No groups found</li>
        {/each}
    </ul>
    {/if}
{/if}
<div class="context-panel-footer">
    <button class="btn btn-skinny"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
</div>


<style>
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>