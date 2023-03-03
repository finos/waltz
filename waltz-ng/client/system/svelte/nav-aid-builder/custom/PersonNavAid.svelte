<script>
    import GroupLeader from "./GroupLeader.svelte";
    import {model} from "./builderStore";
    import Group from "./Group.svelte";
</script>

<div class="navaid-diagram">
    <div class="leaders">
        {#each $model.leaders as leader}
            <div class="leader">
                <GroupLeader leader={leader}
                             scheme="primary"/>
            </div>

        {/each}
    </div>
    <div class="navaid-groups">
        {#each $model.groups as group}
            <Group {group}/>
        {/each}
    </div>
</div>


<style>
    .navaid-groups {
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
    }

    .leaders {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        align-items: stretch;
        align-content: stretch;
        justify-content: space-evenly;
    }

    .leader {
        flex-grow: 1;
        padding: 0.2em;
        flex-basis: 0;
    }

</style>