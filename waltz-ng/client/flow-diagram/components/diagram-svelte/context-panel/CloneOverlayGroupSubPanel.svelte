<script>
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {createEventDispatcher} from "svelte";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import _ from "lodash";

    let workingDiagram;
    export let diagramId;

    let dispatch = createEventDispatcher();

    function cancel() {
        dispatch("cancel")
        workingDiagram = null;
    }

    $: groupsCall = workingDiagram && flowDiagramOverlayGroupStore.findByDiagramId(workingDiagram?.id);

    $: groups = $groupsCall?.data;

    function selectGroup(group){
        flowDiagramOverlayGroupStore.cloneOverlayGroup(diagramId, group.id);
        cancel();
    }

</script>

{#if _.isNil(workingDiagram)}
    <strong>Select a diagram to clone group from:</strong>
    <EntitySearchSelector on:select={e => workingDiagram = e.detail}
                          placeholder="Search for diagram"
                          entityKinds={['FLOW_DIAGRAM']}>
    </EntitySearchSelector>
{:else}
    <strong>Select a group to clone from {workingDiagram.name}</strong>
    {#if groups}
    <ul>
        {#each groups as group}
        <li>
            <button class="btn btn-skinny"
                    on:click={() => selectGroup(group)}>
                {group.name}
            </button>
        </li>
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