<script>
    import GroupLeader from "./GroupLeader.svelte";
    import {model, renderMode, RenderModes as RenderMode} from "./builderStore";

    export let unit = null;
    let people = [];

    $: people = _.filter($model.people, p => p.unitId === unit.unitId);
</script>

<div class="navaid-unit"
     style="border: 1px solid #ccc; margin: 0.5em; background-color: #e5e9fb;">
    <div class="unit-name"
         style="background-color: #0293d0; color: #eee; text-align: center; padding: 0.2em;">
        {unit.name}
    </div>
    <div class="people"
         style=" display: flex; flex-direction: row; justify-content: space-evenly;">
        {#each people as p}
            <div class="person-wrapper"
                 style="padding: 1em;">
                {#if $renderMode === RenderMode.DEV}
                    <GroupLeader leader={p}
                                 scheme="primary"/>
                {:else}
                    <a href="person/id/{p.person?.id}">
                        <GroupLeader leader={p}
                                     scheme="primary"/>
                    </a>
                {/if}
            </div>
        {/each}
    </div>
</div>
