<script>
    import GroupLeader from "./GroupLeader.svelte";
    import {model, renderMode, RenderModes as RenderMode} from "./builderStore";
    import Group from "./Group.svelte";
</script>

<div class="navaid-diagram">
    <div style="display: flex; flex-direction: row; flex-wrap: wrap; align-items: stretch; align-content: stretch; justify-content: space-evenly;"
         class="leaders">
        {#each $model.leaders as leader}
            <div style="flex-grow: 1; padding: 0.2em; flex-basis: 0;"
                 class="leader">
                {#if $renderMode === RenderMode.DEV}
                    <GroupLeader leader={leader}
                                 scheme="primary"/>
                {:else}
                    <a href="person/id/{leader.person?.id}">
                        <GroupLeader leader={leader}
                                     scheme="primary"/>
                    </a>
                {/if}
            </div>
        {/each}
    </div>
    <div class="navaid-groups"
         style="display: flex; flex-direction: column; justify-content: space-evenly;">
        {#each $model.groups as group}
            <Group {group}/>
        {/each}
    </div>
</div>
