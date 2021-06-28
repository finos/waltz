<script>
    import AddNodeSubPanel from "./AddNodeSubPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let canEdit;

    const Modes = {
        MENU: "MENU",
        ADD_NODE: "ADD_NODE",
    };

    const Directions = {
        UPSTREAM: "upstream",
        DOWNSTREAM: "downstream",
    };

    let activeMode = Modes.MENU;

</script>

<div>
    {#if activeMode === Modes.MENU}
        <p class="help-block">Select a node or flow on the diagram to make changes</p>
        {#if canEdit}
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_NODE}>
            <Icon name="plus"/>
            Add node
        </button>
        {/if}
    {:else if activeMode === Modes.ADD_NODE }
        <AddNodeSubPanel on:cancel={() => activeMode = Modes.MENU}/>
    {/if}
</div>

<style>
</style>