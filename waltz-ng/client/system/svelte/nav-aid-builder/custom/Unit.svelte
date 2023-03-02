<script>
    import GroupLeader from "./GroupLeader.svelte";
    import {model} from "./builderStore";

    export let unit = null;
    let people = [];

    $: people = _.filter($model.people, p => p.unitId === unit.unitId);
</script>

<div class="navaid-unit">
    <div class="unit-name">
        {unit.name}
    </div>
    <div class="people">
        {#each people as p}
            <div class="person-wrapper">
                <GroupLeader leader={p}
                             scheme={"secondary"}/>
            </div>
        {/each}
    </div>
</div>

<style>
    .navaid-unit {
        border: 1px solid #ccc;
        margin: 2em 2em;
    }

    .navaid-unit .unit-name {
        background-color: #0b23ea;
        color: #eee;
        text-align: center;
        padding: 0.2em;
    }

    .navaid-unit .people {
        display: flex;
        flex-direction: row;
        justify-content: space-evenly;
    }

    .navaid-unit .person-wrapper {
        padding: 1em;
    }
</style>