<script>
    import {mkRef} from "../../../../common/entity-utils";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {measurableCategoryAlignmentViewStore} from "../../../../svelte-stores/measurable-category-alignment-view-store";
    import OverlayGroupsTable from "./OverlayGroupsTable.svelte";
    import CreateNewOverlayGroupPanel from "./CreateNewOverlayGroupPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    let workingGroup;
    export let diagramId;
    export let canEdit;

    const Modes = {
        TABLE: "TABLE",
        ADD_GROUP: "ADD_GROUP",
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
        <Icon name="plus"/> Add new overlay group
    </button>
    {/if}
{:else if activeMode === Modes.ADD_GROUP}
    <h4>Create Group:</h4>
    <CreateNewOverlayGroupPanel {diagramId}
                                on:cancel={() => activeMode = Modes.TABLE}/>
{/if}

<style>

</style>