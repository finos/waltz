<script>
    import AddNodeSubPanel from "./AddNodeSubPanel.svelte";
    import dirty from "../store/dirty";

    export let doSave;


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

    function saveDiagram(){
        doSave();
    }


</script>

<span>
{#if activeMode === Modes.MENU}
    <button class="btn btn-skinny"
            on:click={() => addNode()}>
        Add node
    </button>
{:else if activeMode === Modes.ADD_NODE }
    <AddNodeSubPanel/>
    <br>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.MENU}>
        Cancel
    </button>
{/if}
    |
    <button class="btn btn-skinny"
            disabled={!$dirty}
            on:click={() => saveDiagram()}>
        Save diagram
    </button>
</span>

<style>
</style>