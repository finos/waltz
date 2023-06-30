<script>

    import {selectedGroup} from "../diagram-builder-store";
    import {DiagramKinds} from "../diagram-builder-utils";
    import GroupControls from "./GroupControls.svelte";
    import ItemControls from "./ItemControls.svelte";

    function determineComp(selected) {
        return selected.kind === DiagramKinds.GROUP
            ? GroupControls
            : ItemControls;
    }

</script>

{#if $selectedGroup}
    <div class="diagram-control-panel">
        <svelte:component this={determineComp($selectedGroup)}/>
    </div>
{:else}
    <div class="help-block">Select an item or group from the diagram or tree to get modify it</div>
{/if}



<style>


    .diagram-control-panel {
    }


</style>