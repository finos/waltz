<script>

    import _ from "lodash";
    import {selectedGroup, workingGroup} from "./diagram-builder-store";

    export let group;

    $: console.log({group, items: group.items});

    let items;

    function editGroup() {
        $selectedGroup = group;
        $workingGroup = group;
    }

    function saveGroup() {
        console.log({sg: $selectedGroup, wg: $workingGroup});
        group = $workingGroup;
        $selectedGroup = null;
        $workingGroup = null;
    }

</script>

<div style="height: 100%; width: 100%">
    {#if !_.isEmpty(group.title)}
        <div class="diagram-title">
            {group.title}
            {#if _.isNil($selectedGroup)}
                <button class="btn btn-skinny"
                        on:click={() => editGroup(group)}>
                    Edit Group
                </button>
            {/if}
            {#if $selectedGroup === group}
                <button class="btn btn-skinny"
                        on:click={() => saveGroup(group)}>
                    Save Group
                </button>
            {/if}
        </div>
    {/if}

    {#if !_.isEmpty(group.items)}
        <div class={`diagram-container diagram-container-${group.props.flexDirection}`}>
            {#each group.items as item}
                <div class="item">
                    <svelte:self group={item}>
                    </svelte:self>
                </div>
            {/each}
        </div>
    {/if}
</div>


<style>

    .diagram-container {

        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
        align-content: flex-start;
        align-items: center;
        gap: 0.5em;

        border: 1px solid red;
        background-color: antiquewhite;

        height: inherit;
    }

    .diagram-container-row {
        flex-direction: row;
    }

    .diagram-container-row > .item {
        flex: 0 1 10em; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-column {
        flex-direction: column;
    }

    .diagram-container-column > .item {
        flex: 1 1 5em; /* when columns this sets the height*/
        width: fit-content;
        min-width: 10em;
    }

    .item {
        border: 1px solid blue;
        background-color: #d7f4fa;
        margin: 0.5em;
    }

    .diagram-title {
        text-align: center;
        background-color: #0b23ea;
        font-weight: bolder;
        color: white;
    }

</style>