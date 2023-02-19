<script>
    import Unit from "./Unit.svelte";
    export let group = null;
</script>


<div class="group">
    <div class="group-name">
        {group.name}
    </div>
    <div class="units">
        {#each group.units as unit}
            <div class="unit-wrapper">
                <Unit {unit}/>
            </div>
        {/each}
    </div>
</div>

<style>
    .group {
        border: 1px solid #ccc;
        margin: 1em 0;

    }

    .group-name {
        text-align: center;
        background-color: #eee;
        padding: 0.3em;
        width: 100%;
    }

    .unit-wrapper {
        padding: 0.2em;
        flex-basis: 33%;
        flex-grow: 1;
    }

    .units {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        align-items: stretch;
        align-content: stretch;
        justify-content: space-evenly;
    }
</style>