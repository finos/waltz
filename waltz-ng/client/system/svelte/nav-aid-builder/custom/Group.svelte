<script>
    import Unit from "./Unit.svelte";
    import {model} from "./builderStore";
    export let group = null;

    let units = [];

    $: units = _.filter($model.units, u => u.groupId === group.groupId);

</script>


<div class="navaid-group">
    <div class="group-name">
        {group.name}
    </div>
    <div class="units">
        {#each units as unit}
            <div class="unit-wrapper">
                <Unit {unit}/>
            </div>
        {/each}
    </div>
</div>

<style>
    .navaid-group {
        border: 1px solid #c5c9dc;
        margin: 1em 0;
    }

    .navaid-group .group-name {
        text-align: center;
        background-color: #e5e9fb;
        padding: 0.3em;
        width: 100%;
    }

    .navaid-group .unit-wrapper {
        padding: 0.2em;
        flex-basis: 33%;
        flex-grow: 1;
    }

    .navaid-group .units {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        align-items: stretch;
        align-content: stretch;
        justify-content: space-evenly;
    }
</style>