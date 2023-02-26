<script>
    import GroupLeader from "./GroupLeader.svelte";

    export let unit = null;
</script>

<div class="navaid-unit">
    <div class="unit-name">
        {unit.name}
    </div>
    <div class="people">
        {#each unit.people as p}
            <div class="person-wrapper">
                <GroupLeader title={p.title}
                             person={p.person}/>
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