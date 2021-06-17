<script>
    import AddNodeSubPanel from "./AddNodeSubPanel.svelte";

    export let canEdit;

    const Modes = {
        MENU: "MENU",
        ADD_NODE: "ADD_NODE",
        ADD_UPSTREAM_FLOW: "ADD_UPSTREAM_FLOW",
        ADD_DOWNSTREAM_FLOW: "ADD_DOWNSTREAM_FLOW",
    };

    const Directions = {
        UPSTREAM: "upstream",
        DOWNSTREAM: "downstream",
    };

    let activeMode = Modes.MENU;


    function addNode() {
        activeMode = Modes.ADD_NODE
    }

</script>

<div>
    {#if activeMode === Modes.MENU}
        {#if canEdit}
        <button class="btn btn-skinny"
                on:click={() => addNode()}>
            Add node
        </button>
        {/if}
    {:else if activeMode === Modes.ADD_NODE }
        <AddNodeSubPanel/>
        <br>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.MENU}>
            Cancel
        </button>
    {/if}
</div>

<style>
</style>