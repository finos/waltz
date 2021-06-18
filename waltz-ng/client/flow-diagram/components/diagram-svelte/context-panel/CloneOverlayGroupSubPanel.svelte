<script>
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {createEventDispatcher} from "svelte";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";

    let workingGroup;
    let workingDiagram;
    export let diagramId;
    export let canEdit;

    let dispatch = createEventDispatcher();

    function cancel() {
        dispatch("cancel")
        workingDiagram = null;
    }

    $: console.log({workingDiagram});

    $: groupsCall = workingDiagram && flowDiagramOverlayGroupStore.findByDiagramId(workingDiagram?.id);

    $: groups = $groupsCall?.data;

    function selectGroup(group){
        flowDiagramOverlayGroupStore.cloneOverlayGroup(diagramId, group.id);
        cancel();
    }

    $: console.log({groups});

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
            <button on:click={() => selectGroup(group)}>{group.name}</button></li>
        {/each}
    </ul>
    {/if}
{/if}

<style>

</style>