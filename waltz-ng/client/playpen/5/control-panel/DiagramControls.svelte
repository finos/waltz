<script>

    import {selectedGroup} from "../diagram-builder-store";
    import GroupControls from "./GroupControls.svelte";
</script>

{#if $selectedGroup}
    <div class="diagram-control-panel">
        <GroupControls/>
    </div>
{:else}
    <div class="help-block">Select an item or group from the diagram or tree to get modify it</div>
{/if}



<style>

    .diagram-control-panel {
    }

</style>