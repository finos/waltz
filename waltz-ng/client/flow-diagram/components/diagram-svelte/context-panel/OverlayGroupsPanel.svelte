<script>
    import OverlayGroupsTable from "./OverlayGroupsTable.svelte";
    import CreateNewOverlayGroupPanel from "./CreateNewOverlayGroupPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import CloneOverlayGroupSubPanel from "./CloneOverlayGroupSubPanel.svelte";

    let workingGroup;
    export let diagramId;
    export let canEdit;

    const Modes = {
        TABLE: "TABLE",
        ADD_GROUP: "ADD_GROUP",
        CLONE_GROUP: "CLONE_GROUP",
    };

    let activeMode = Modes.TABLE

    function cancel() {
        workingGroup = null;
    }

</script>

{#if activeMode === Modes.TABLE}
    <OverlayGroupsTable {diagramId} {canEdit}/>
    {#if canEdit}
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.ADD_GROUP}>
        <Icon name="plus"/>Add new overlay group
    </button>
    |
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.CLONE_GROUP}>
        <Icon name="clone"/>Import overlay group
    </button>
    {/if}
{:else if activeMode === Modes.ADD_GROUP}
    <h4>Create Group:</h4>
    <CreateNewOverlayGroupPanel {diagramId}
                                on:cancel={() => activeMode = Modes.TABLE}/>
{:else if activeMode === Modes.CLONE_GROUP}
    <h4>Clone Group from:</h4>
    <CloneOverlayGroupSubPanel {diagramId} on:cancel={() => activeMode = Modes.TABLE}/>
{/if}

<style>

</style>